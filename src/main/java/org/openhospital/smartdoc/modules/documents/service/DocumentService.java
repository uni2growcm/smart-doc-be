package org.openhospital.smartdoc.modules.documents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.mapper.DocumentMapper;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.repository.DocumentRepository;
import org.openhospital.smartdoc.modules.documents.repository.DocumentTypeRepository;
import org.openhospital.smartdoc.modules.persons.repository.PersonRepository;
import org.openhospital.smartdoc.modules.shared.port.IUploadService;
import org.openhospital.smartdoc.openapi.DocumentDTO;
import org.openhospital.smartdoc.openapi.DocumentStatus;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service implementation for document operations.
 * Handles file uploads, downloads, and document management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService implements IDocumentService {

	private final DocumentRepository repository;
	private final DocumentTypeRepository documentTypeRepository;
	private final PersonRepository personRepository;
	private final IUploadService uploadService;
	private final DocumentMapper mapper;

	private Document findById(UUID id) {
		return repository.findById(id).orElseThrow(() -> CustomException.notFound("documents.errors.not-found", new Object[]{id}));
	}


	private Document findByIdAndStatusNot(UUID id, DocumentStatus status) {
		return repository.findByIdAndStatusNot(id, status).orElseThrow(() -> CustomException.notFound("documents.errors.not-found", new Object[]{id}));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentDTO> findDocuments(UUID personId, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		log.debug("Finding documents with filters - personId: {}, type: {}, date range: {} to {}, page: {}, size: {}", personId, type, fromDate, toDate, page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

			// Convert LocalDate to Instant for database query
			Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
			Instant toInstant = toDate != null ? toDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC) : null;

			var documentPage = repository.findWithFilters(personId, type, fromInstant, toInstant, pageable);

			Page<DocumentDTO> result = Page.from(documentPage, mapper::toDto);

			log.info("Found {} documents (page {}/{}, total: {})", result.getData().size(), page, documentPage.getTotalPages(), documentPage.getTotalElements());
			return result;
		} catch (Exception e) {
			log.error("Failed to find documents", e);
			throw CustomException.internal("documents.errors.search-failed");
		}
	}


	@Override
	@Transactional
	public DocumentDTO uploadDocument(MultipartFile document, UUID personId, UUID typeId, LocalDate date, String description) {
		log.info("Uploading document for person: {}, type: {}", personId, typeId);

		// Validate references exist
		validatePersonReference(personId);
		DocumentType documentType = validateDocumentTypeReference(typeId);

		try {
			// Validate file
			uploadService.validateFile(document);

			// Store file
			String subDir = getSubDirForDocumentType(documentType.getCode());
			String storedPath = uploadService.uploadFile(document, personId, subDir);

			// Create document entity
			Document entity = new Document();
			entity.setFileName(document.getOriginalFilename());
			entity.setPath(storedPath);
			entity.setPerson(personRepository.findById(personId).orElseThrow());
			entity.setType(documentType);
			entity.setDate(date != null ? date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : Instant.now());
			entity.setDescription(description);
			entity.setFileSize(document.getSize());
			entity.setMimeType(document.getContentType());
			entity.setStatus(DocumentStatus.ACTIVE);
			entity.setUploadDate(Instant.now());

			Document saved = repository.save(entity);
			DocumentDTO result = mapper.toDto(saved);

			log.info("Document uploaded successfully with ID: {} for person: {}", saved.getId(), personId);
			return result;
		} catch (IOException e) {
			log.error("File upload failed for person: {}", personId, e);
			throw CustomException.internal("documents.errors.upload-failed");
		} catch (Exception e) {
			log.error("Document upload failed", e);
			throw CustomException.internal("documents.errors.upload-failed");
		}
	}


	@Override
	@Transactional(readOnly = true)
	public DocumentDTO findDocumentById(UUID id) {
		log.debug("Retrieving document metadata by ID: {}", id);

		Document document = findByIdAndStatusNot(id, DocumentStatus.DELETED);

		DocumentDTO result = mapper.toDto(document);
		log.debug("Document metadata retrieved successfully: {}", document.getFileName());
		return result;
	}


	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<ByteArrayResource> downloadDocument(UUID id, boolean attachment) {
		log.debug("Downloading document by ID: {}, attachment: {}", id, attachment);

		Document document = findByIdAndStatusNot(id, DocumentStatus.DELETED);

		return uploadService.downloadFile(document.getPath(), attachment);
	}


	@Override
	@Transactional
	public DocumentDTO updateDocument(UUID id, MultipartFile document, UUID personId, UUID typeId, LocalDate date, String description) {
		log.info("Updating document with ID: {}", id);

		Document existing = findById(id);

		try {
			boolean fileChanged = document != null && !document.isEmpty();

			if (fileChanged) {
				// Validate new file
				uploadService.validateFile(document);

				// Delete old file
				uploadService.deleteFile(existing.getPath());

				// Store new file
				DocumentType documentType = existing.getType();
				if (typeId != null) {
					documentType = validateDocumentTypeReference(typeId);
				}
				String subDir = getSubDirForDocumentType(documentType.getCode());
				String newPath = uploadService.uploadFile(document, existing.getPerson().getId(), subDir);

				existing.setFileName(document.getOriginalFilename());
				existing.setPath(newPath);
				existing.setFileSize(document.getSize());
				existing.setMimeType(document.getContentType());
			}

			// Update metadata
			if (personId != null) {
				validatePersonReference(personId);
				existing.setPerson(personRepository.findById(personId).orElseThrow());
			}
			if (typeId != null) {
				existing.setType(validateDocumentTypeReference(typeId));
			}
			if (date != null) {
				existing.setDate(date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
			}
			if (description != null) {
				existing.setDescription(description);
			}

			Document saved = repository.save(existing);
			DocumentDTO result = mapper.toDto(saved);

			log.info("Document updated successfully: {}", saved.getId());
			return result;
		} catch (IOException e) {
			log.error("File update failed for document: {}", id, e);
			throw CustomException.internal("documents.errors.update-failed");
		} catch (Exception e) {
			log.error("Failed to update document", e);
			throw CustomException.internal("documents.errors.update-failed");
		}
	}


	@Override
	@Transactional
	public void deleteDocument(UUID id) {
		log.info("Deleting document with ID: {}", id);

		Document existing = findById(id);

		// Delete file from storage
		boolean fileDeleted = uploadService.deleteFile(existing.getPath());
		if (!fileDeleted) {
			log.warn("File not found during deletion: {}", existing.getPath());
		}

		// Delete from database
		repository.deleteById(id);

		log.info("Document deleted successfully: {}", existing.getFileName());
	}

	/**
	 * Validates person reference for operations.
	 */
	private void validatePersonReference(UUID personId) {
		if (!personRepository.existsById(personId)) {
			throw CustomException.notFound("persons.errors.not-found", new Object[]{personId});
		}
	}

	/**
	 * Validates document type reference for operations.
	 */
	private DocumentType validateDocumentTypeReference(UUID typeId) {
		return documentTypeRepository.findById(typeId).orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{typeId}));
	}

	/**
	 * Determines the subdirectory based on document type code.
	 */
	private String getSubDirForDocumentType(String typeCode) {
		if (typeCode == null) return "documents";

		return switch (typeCode.toLowerCase()) {
			case "pdf", "doc", "docx", "txt", "rtf", "odt" -> "documents";
			case "jpg", "jpeg", "png", "gif", "bmp", "tiff" -> "images";
			case "mp4", "avi", "mov", "wmv", "flv", "webm" -> "videos";
			default -> "documents";
		};
	}
}