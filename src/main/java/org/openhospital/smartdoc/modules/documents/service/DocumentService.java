package org.openhospital.smartdoc.modules.documents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.NumberUtils;
import org.openhospital.smartdoc.modules.documents.mapper.DocumentMapper;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.repository.DocumentTypeRepository;
import org.openhospital.smartdoc.modules.persons.repository.PersonRepository;
import org.openhospital.smartdoc.modules.shared.port.IUploadService;
import org.openhospital.smartdoc.modules.shared.properties.StorageProperties;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.openhospital.smartdoc.openapi.Status;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation for document operations.
 * Handles file uploads, downloads, and document management using filesystem-based metadata.
 */
@Slf4j
@Service(DocumentService.NAME)
@RequiredArgsConstructor
public class DocumentService implements IDocumentService {

	public static final String NAME = "DocumentService";

	private final DocumentTypeRepository documentTypeRepository;
	private final PersonRepository personRepository;
	private final IUploadService uploadService;
	private final StorageProperties storageProperties;
	private final DocumentMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentResponse> findDocuments(int personId, String type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		log.debug("Finding documents with filters - personId: {}, type: {}, date range: {} to {}, page: {}, size: {}", personId, type, fromDate, toDate, page, size);

		ensurePersonExists(personId);

		try {
			String personPath = NumberUtils.toSixDigitPath(personId);
			String baseDir = storageProperties.paths().baseDir();
			Path basePath = Paths.get(baseDir, personPath);
			Pageable pageable = PageRequest.of(page, size);


			if (!Files.exists(basePath) || !Files.exists(basePath)) {
				return Page.from(new PageImpl<>(Collections.emptyList(), pageable, 0), Function.identity());
			}

			List<DocumentResponse> allDocuments;
			try (var paths = Files.walk(basePath)) {
				allDocuments = paths
					.filter(Files::isRegularFile)
					.map(filePath -> {
						try {
							return mapper.toDto(filePath, baseDir);
						} catch (Exception e) {
							log.warn("Failed to parse document path: {}", filePath, e);
							return null;
						}
					})
					.filter(java.util.Objects::nonNull)
					.filter(doc -> type == null || type.equals(doc.getType()))
					.filter(doc -> {
						if (fromDate == null && toDate == null) return true;
						assert doc.getId() != null;
						LocalDate docDate = parseDateFromId(doc.getId());
						if (fromDate != null && docDate.isBefore(fromDate)) return false;
						return toDate == null || !docDate.isAfter(toDate);
					})
					.sorted(Comparator.comparing((DocumentResponse doc) -> {
						assert doc.getId() != null;
						return parseDateFromId(doc.getId());
					}).reversed())
					.collect(Collectors.toList());
			}

			int totalElements = allDocuments.size();
			int start = page * size;
			int end = Math.min(start + size, totalElements);
			List<DocumentResponse> pageData = allDocuments.subList(start, end);

			org.springframework.data.domain.Page<DocumentResponse> documentsPage = new PageImpl<>(pageData, pageable, totalElements);
			Page<DocumentResponse> result = Page.from(documentsPage, Function.identity());

			log.info("Found {} documents (page {}/{}, total: {})", result.getData().size(), page, result.getMetadata().getTotalPages(), result.getMetadata().getTotalElements());
			return result;
		} catch (Exception e) {
			log.error("Failed to find documents", e);
			throw CustomException.internal("documents.errors.search-failed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<ByteArrayResource> downloadDocument(String id, int personId, String type, boolean attachment) {
		log.debug("Downloading document by ID: {}, attachment: {}", id, attachment);

		ensurePersonExists(personId);

		return uploadService.downloadFile(resolveDocumentPath(id, personId, type), attachment);
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentResponse findDocumentById(String id, int personId, String type) {
		log.debug("Finding document by ID: {}", id);

		ensurePersonExists(personId);

		String baseDir = storageProperties.paths().baseDir();
		Path filePath = Paths.get(baseDir, resolveDocumentPath(id, personId, type));

		if (!Files.exists(filePath)) {
			throw CustomException.notFound("documents.errors.not-found", new Object[]{id});
		}

		return mapper.toDto(filePath, baseDir);
	}

	@Override
	@Transactional
	public DocumentResponse uploadDocument(MultipartFile document, int personId, String type, LocalDate date) {
		log.info("Uploading document for client: {}, type: {}", personId, type);

		// Validate references exist
		validatePersonReference(personId);
		DocumentType documentType = validateDocumentTypeReference(type);

		try {
			// Validate file
			uploadService.validateFile(document);

			String subDir = NumberUtils.toSixDigitPath(personId) + "/" + documentType.getCode();
			String storedPath = uploadService.uploadFile(document, subDir, date);

			Path filePath = Paths.get(storageProperties.paths().baseDir(), storedPath);
			DocumentResponse result = mapper.toDto(filePath, storageProperties.paths().baseDir());

			log.info("Document uploaded successfully with ID: {} for client: {}", result.getId(), personId);
			return result;
		} catch (IOException e) {
			log.error("File upload failed for client: {}", personId, e);
			throw CustomException.internal("documents.errors.upload-failed");
		} catch (Exception e) {
			log.error("Document upload failed", e);
			throw CustomException.internal("documents.errors.upload-failed");
		}
	}

	/**
	 * Parses the date from a document ID (relative path).
	 */
	private LocalDate parseDateFromId(String id) {
		int underscoreIndex = id.indexOf('_');
		if (underscoreIndex == -1) {
			throw new IllegalArgumentException("Invalid filename in ID: " + id);
		}
		String dateStr = id.substring(0, underscoreIndex);
		return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * Validates client reference for operations.
	 */
	private void validatePersonReference(int personId) {
		if (!personRepository.existsByPidAndStatusNot(personId, Status.DELETED)) {
			throw CustomException.notFound("persons.errors.not-found", new Object[]{personId});
		}
	}

	/**
	 * Validates document type reference for operations.
	 */
	private DocumentType validateDocumentTypeReference(String typeId) {
		return documentTypeRepository.findByCode(typeId).orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{typeId}));
	}

	private String resolveDocumentPath(String id, int personId, String type) {
		return "%s/%s/%s".formatted(NumberUtils.toSixDigitPath(personId), type, id);
	}

	private void ensurePersonExists(int personId) {
		if (!personRepository.existsByPidAndStatusNot(personId, Status.DELETED)) {
			log.warn("Person with ID {} not found or deleted", personId);
			throw CustomException.badRequest("persons.errors.not-found", new Object[]{personId});
		}
	}
}