package org.openhospital.smartdoc.modules.documents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
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

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentDTO> findDocuments(
		UUID personId, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size
	                                      ) {
		log.debug("Finding documents with filters - personId: {}, type: {}, date range: {} to {}, page: {}, size: {}",
			personId, type, fromDate, toDate, page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

			// Convert LocalDate to Instant for database query
			Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
			Instant toInstant = toDate != null ? toDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC) : null;

			var documentPage = repository.findWithFilters(
				personId, type, fromInstant, toInstant, pageable);

			Page<DocumentDTO> result = Page.from(documentPage, documents ->
				documents.stream().map(doc -> {
					DocumentDTO dto = new DocumentDTO();
					dto.setId(doc.getId());
					dto.setFileName(doc.getFileName());
					dto.setPath(doc.getPath());
					dto.setPersonId(doc.getPerson().getId().toString());
					dto.setType(doc.getType().getId().toString());
					dto.setDate(doc.getDate());
					dto.setDescription(doc.getDescription());
					dto.setFileSize(doc.getFileSize() != null ? doc.getFileSize().intValue() : null);
					dto.setMimeType(doc.getMimeType());
					dto.setStatus(doc.getStatus());
					dto.setUploadDate(doc.getUploadDate());
					return dto;
				}).toList());

			log.info("Found {} documents (page {}/{}, total: {})",
				result.getData().size(), page, documentPage.getTotalPages(), documentPage.getTotalElements());
			return result;
		} catch (Exception e) {
			log.error("Failed to find documents", e);
			throw CustomException.internal("documents.errors.search-failed");
		}
	}

	@Override
	@Transactional
	public DocumentDTO uploadDocument(
		MultipartFile document, UUID personId, UUID typeId, LocalDate date, String description
	                                 ) {
		log.info("Uploading document for person: {}, type: {}", personId, typeId);

		// Validate references exist
		validatePersonExists(personId);
		DocumentType documentType = validateDocumentTypeExists(typeId);

		try {
			// Validate file
			uploadService.validateFile(document);

			// Store file
			String subDir = getSubDirForDocumentType(documentType.getCode());
			String storedPath = uploadService.storeFile(document, personId, subDir);

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
			DocumentDTO result = new DocumentDTO();
			result.setId(saved.getId());
			result.setFileName(saved.getFileName());
			result.setPath(saved.getPath());
			result.setPersonId(saved.getPerson().getId().toString());
			result.setType(saved.getType().getId().toString());
			result.setDate(saved.getDate());
			result.setDescription(saved.getDescription());
			result.setFileSize(saved.getFileSize() != null ? saved.getFileSize().intValue() : null);
			result.setMimeType(saved.getMimeType());
			result.setStatus(saved.getStatus());
			result.setUploadDate(saved.getUploadDate());

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
	public ResponseEntity<byte[]> findDocumentById(UUID id) {
		log.debug("Retrieving document by ID: {}", id);

		Document document = repository.findById(id)
		                              .orElseThrow(() -> {
			                              log.warn("Document not found with ID: {}", id);
			                              return CustomException.notFound("documents.errors.not-found", new Object[]{id});
			                              });

		try {
			Resource resource = uploadService.retrieveFile(document.getPath());

			if (!resource.exists()) {
				log.warn("Document file not found on disk: {}", document.getPath());
				throw CustomException.notFound("documents.errors.file-not-found");
			}

			byte[] content = resource.getInputStream().readAllBytes();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(document.getMimeType() != null ?
				document.getMimeType() : "application/octet-stream"));
			headers.setContentDisposition(
				org.springframework.http.ContentDisposition.attachment()
				                                           .filename(document.getFileName())
				                                           .build()
			                             );
			headers.setContentLength(content.length);

			log.debug("Document retrieved successfully: {}", document.getFileName());
			return ResponseEntity.ok()
			                     .headers(headers)
			                     .body(content);
		} catch (IOException e) {
			log.error("Failed to read document file: {}", document.getPath(), e);
			throw CustomException.internal("documents.errors.read-failed");
		}
	}

	@Override
	@Transactional
	public DocumentDTO updateDocument(
		UUID id, MultipartFile document, UUID personId, UUID typeId, LocalDate date, String description
	                                 ) {
		log.info("Updating document with ID: {}", id);

		Document existing = repository.findById(id)
		                              .orElseThrow(() -> {
			                              log.warn("Document not found for update with ID: {}", id);
			                              return CustomException.notFound("documents.errors.not-found", new Object[]{id});
			                              });

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
					documentType = validateDocumentTypeExists(typeId);
				}
				String subDir = getSubDirForDocumentType(documentType.getCode());
				String newPath = uploadService.storeFile(document, existing.getPerson().getId(), subDir);

				existing.setFileName(document.getOriginalFilename());
				existing.setPath(newPath);
				existing.setFileSize(document.getSize());
				existing.setMimeType(document.getContentType());
			}

			// Update metadata
			if (personId != null) {
				validatePersonExists(personId);
				existing.setPerson(personRepository.findById(personId).orElseThrow());
			}
			if (typeId != null) {
				existing.setType(validateDocumentTypeExists(typeId));
			}
			if (date != null) {
				existing.setDate(date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
			}
			if (description != null) {
				existing.setDescription(description);
			}

			Document saved = repository.save(existing);
			DocumentDTO result = new DocumentDTO();
			result.setId(saved.getId());
			result.setFileName(saved.getFileName());
			result.setPath(saved.getPath());
			result.setPersonId(saved.getPerson().getId().toString());
			result.setType(saved.getType().getId().toString());
			result.setDate(saved.getDate());
			result.setDescription(saved.getDescription());
			result.setFileSize(saved.getFileSize() != null ? saved.getFileSize().intValue() : null);
			result.setMimeType(saved.getMimeType());
			result.setStatus(saved.getStatus());
			result.setUploadDate(saved.getUploadDate());

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

		Document existing = repository.findById(id)
		                              .orElseThrow(() -> {
			                              log.warn("Document not found for deletion with ID: {}", id);
			                              return CustomException.notFound("documents.errors.not-found", new Object[]{id});
			                              });

		try {
			// Delete file from storage
			boolean fileDeleted = uploadService.deleteFile(existing.getPath());
			if (!fileDeleted) {
				log.warn("File not found during deletion: {}", existing.getPath());
			}

			// Delete from database
			repository.deleteById(id);

			log.info("Document deleted successfully: {}", existing.getFileName());
		} catch (Exception e) {
			log.error("Failed to delete document", e);
			throw CustomException.internal("documents.errors.deletion-failed");
		}
	}

	/**
	 * Validates that a person exists.
	 */
	private void validatePersonExists(UUID personId) {
		if (!personRepository.existsById(personId)) {
			throw CustomException.notFound("persons.errors.not-found", new Object[]{personId});
		}
	}

	/**
	 * Validates that a document type exists and returns it.
	 */
	private DocumentType validateDocumentTypeExists(UUID typeId) {
		return documentTypeRepository.findById(typeId)
		                             .orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{typeId}));
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