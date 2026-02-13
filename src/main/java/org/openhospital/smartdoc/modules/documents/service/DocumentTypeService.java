package org.openhospital.smartdoc.modules.documents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.mapper.DocumentTypeMapper;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.modules.documents.port.IDocumentTypeService;
import org.openhospital.smartdoc.modules.documents.repository.DocumentTypeRepository;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for document type operations.
 * Provides CRUD operations for document type management.
 */
@Slf4j
@Service(DocumentTypeService.NAME)
@RequiredArgsConstructor
public class DocumentTypeService implements IDocumentTypeService {

	public static final String NAME = "DocumentTypeService";

	private final DocumentTypeRepository repository;
	private final DocumentTypeMapper mapper;

	private DocumentType findById(UUID id) {
		return repository.findById(id).orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id}));
	}

	private DocumentType findNotDeletedById(UUID id) {
		return repository.findByIdAndStatusNot(id, Status.DELETED).orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id}));
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTypeResponse> findDocumentTypes(boolean includeInactive) {
		log.debug("Fetching all document types, includeInactive: {}", includeInactive);

		try {
			List<Status> statuses = includeInactive ? List.of(Status.ACTIVE, Status.INACTIVE) : List.of(Status.ACTIVE);

			Page<DocumentType> documentTypePage = repository.findByStatusIn(statuses, Pageable.unpaged());
			List<DocumentType> documentTypes = documentTypePage.getContent();
			List<DocumentTypeResponse> dtos = mapper.toDtos(documentTypes);

			log.info("Retrieved {} document types", dtos.size());
			return dtos;
		} catch (Exception e) {
			log.error("Failed to retrieve document types", e);
			throw CustomException.internal("documents.errors.types-fetch-failed");
		}
	}

	@Override
	@Transactional
	public DocumentTypeResponse createDocumentType(CreateDocumentTypeRequest payload) {
		log.info("Creating document type with code: {}", payload.getCode());

		validateDocumentTypeRequest(payload);

		DocumentType entity = mapper.toModel(payload);
		validateDocumentType(entity);  // Entity validation with business rules
		DocumentType saved = repository.save(entity);
		DocumentTypeResponse result = mapper.toDto(saved);

		log.info("Document type created successfully with ID: {}", saved.getId());
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentTypeResponse findDocumentTypeById(UUID id) {
		log.debug("Finding document type by ID: {}", id);

		DocumentType entity = findNotDeletedById(id);

		DocumentTypeResponse result = mapper.toDto(entity);
		log.debug("Document type found: {}", entity.getCode());
		return result;
	}

	@Override
	@Transactional
	public DocumentTypeResponse updateDocumentType(UUID id, UpdateDocumentTypeRequest payload) {
		log.info("Updating document type with ID: {}", id);

		DocumentType existing = findNotDeletedById(id);

		mapper.updateModel(payload, existing);
		DocumentType saved = repository.save(existing);
		DocumentTypeResponse result = mapper.toDto(saved);

		log.info("Document type updated successfully: {}", saved.getCode());
		return result;
	}

	@Override
	@Transactional
	public DocumentTypeResponse patchDocumentType(UUID id, PatchDocumentTypeRequest payload) {
		log.info("Patching document type with ID: {}", id);

		DocumentType existing = findNotDeletedById(id);

		mapper.patchModel(payload, existing);
		DocumentType saved = repository.save(existing);
		DocumentTypeResponse result = mapper.toDto(saved);

		log.info("Document type patched successfully: {}", saved.getCode());
		return result;
	}

	@Transactional
	public void deleteDocumentType(UUID id) {
		log.info("Deleting document type with ID: {}", id);

		DocumentType documentType = findNotDeletedById(id);

		documentType.setStatus(Status.DELETED);
		repository.save(documentType);

		log.info("Document type {} soft deleted successfully", id);
	}

	@Override
	@Transactional
	public DocumentTypeResponse activateDocumentType(UUID id) {
		log.info("Activating document type with ID: {}", id);

		DocumentType documentType = findNotDeletedById(id);

		if (documentType.getStatus() == Status.ACTIVE) {
			throw CustomException.badRequest("documents.errors.type-already-active");
		}

		documentType.setStatus(Status.ACTIVE);
		DocumentType saved = repository.save(documentType);
		log.info("Document type {} activated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional
	public DocumentTypeResponse deactivateDocumentType(UUID id) {
		log.info("Deactivating document type with ID: {}", id);

		DocumentType documentType = findNotDeletedById(id);

		if (documentType.getStatus() == Status.INACTIVE) {
			throw CustomException.badRequest("documents.errors.type-already-inactive");
		}

		documentType.setStatus(Status.INACTIVE);
		DocumentType saved = repository.save(documentType);
		log.info("Document type {} deactivated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional
	public DocumentTypeResponse restoreDocumentType(UUID id) {
		log.info("Restoring document type with ID: {}", id);

		DocumentType documentType = findById(id);

		if (documentType.getStatus() != Status.DELETED) {
			throw CustomException.badRequest("documents.errors.type-not-deleted");
		}

		documentType.setStatus(Status.ACTIVE);
		DocumentType saved = repository.save(documentType);
		log.info("Document type {} restored and reactivated successfully", id);
		return mapper.toDto(saved);
	}

	/**
	 * Validates document type entity with business rules.
	 */
	public void validateDocumentType(DocumentType documentType) {
		// Code uniqueness check (excluding DELETED)
		if (documentType.getId() == null && documentType.getCode() != null) {
			if (repository.existsByCodeAndStatusNot(documentType.getCode(), Status.DELETED)) {
				throw CustomException.badRequest("documents.errors.type-code-already-exists", new Object[]{documentType.getCode()});
			}
		}

		// Business rules
		if (documentType.getCode() == null || documentType.getCode().trim().length() < 2) {
			throw CustomException.badRequest("documents.errors.type-code-too-short");
		}

		if (documentType.getName() == null || documentType.getName().trim().length() < 2) {
			throw CustomException.badRequest("documents.errors.type-name-too-short");
		}
	}

	/**
	 * Validates the create request payload.
	 */
	public void validateDocumentTypeRequest(CreateDocumentTypeRequest payload) {
		if (payload.getCode() == null || payload.getCode().trim().isEmpty()) {
			throw CustomException.badRequest("documents.errors.type-code-required");
		}

		if (payload.getName() == null || payload.getName().trim().isEmpty()) {
			throw CustomException.badRequest("documents.errors.type-name-required");
		}
	}
}