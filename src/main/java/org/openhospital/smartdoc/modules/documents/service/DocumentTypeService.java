package org.openhospital.smartdoc.modules.documents.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.mapper.DocumentTypeMapper;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.modules.documents.port.IDocumentTypeService;
import org.openhospital.smartdoc.modules.documents.repository.DocumentTypeRepository;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.dao.DataIntegrityViolationException;
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

	private DocumentType findByIdAndStatusNot(UUID id, Status status) {
		return repository.findByIdAndStatusNot(id, status).orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id}));
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTypeDTO> findDocumentTypes(boolean includeInactive) {
		log.debug("Fetching all document types, includeInactive: {}", includeInactive);

		try {
			List<Status> statuses = includeInactive ? List.of(Status.ACTIVE, Status.INACTIVE) : List.of(Status.ACTIVE);

			Page<DocumentType> documentTypePage = repository.findByStatusIn(statuses, Pageable.unpaged());
			List<DocumentType> documentTypes = documentTypePage.getContent();
			List<DocumentTypeDTO> dtos = documentTypes.stream().map(mapper::toDto).toList();

			log.info("Retrieved {} document types", dtos.size());
			return dtos;
		} catch (Exception e) {
			log.error("Failed to retrieve document types", e);
			throw CustomException.internal("documents.errors.types-fetch-failed");
		}
	}

	@Override
	@Transactional
	public DocumentTypeDTO createDocumentType(CreateDocumentTypeRequestDTO payload) {
		log.info("Creating document type with code: {}", payload.getCode());

		validateDocumentTypeRequest(payload);

		DocumentType entity = mapper.toModel(payload);
		validateDocumentType(entity);  // Entity validation with business rules
		DocumentType saved = repository.save(entity);
		DocumentTypeDTO result = mapper.toDto(saved);

		log.info("Document type created successfully with ID: {}", saved.getId());
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentTypeDTO findDocumentTypeById(UUID id) {
		log.debug("Finding document type by ID: {}", id);

		DocumentType entity = findByIdAndStatusNot(id, Status.DELETED);

		DocumentTypeDTO result = mapper.toDto(entity);
		log.debug("Document type found: {}", entity.getCode());
		return result;
	}

	@Override
	@Transactional
	public DocumentTypeDTO updateDocumentType(UUID id, UpdateDocumentTypeRequestDTO payload) {
		log.info("Updating document type with ID: {}", id);

		DocumentType existing = findByIdAndStatusNot(id, Status.DELETED);

		mapper.updateModel(payload, existing);
		DocumentType saved = repository.save(existing);
		DocumentTypeDTO result = mapper.toDto(saved);

		log.info("Document type updated successfully: {}", saved.getCode());
		return result;
	}

	@Override
	@Transactional
	public DocumentTypeDTO patchDocumentType(UUID id, PatchDocumentTypeRequestDTO payload) {
		log.info("Patching document type with ID: {}", id);

		DocumentType existing = findByIdAndStatusNot(id, Status.DELETED);

		mapper.patchModel(payload, existing);
		DocumentType saved = repository.save(existing);
		DocumentTypeDTO result = mapper.toDto(saved);

		log.info("Document type patched successfully: {}", saved.getCode());
		return result;
	}

	@Transactional
	public void deleteDocumentType(UUID id) {
		log.info("Deleting document type with ID: {}", id);

		DocumentType documentType = findByIdAndStatusNot(id, Status.DELETED);

		try {
			// Try hard delete first
			repository.deleteById(id);
			log.info("Document type {} hard deleted successfully", id);
		} catch (DataIntegrityViolationException e) {
			// Foreign key constraint violation - document type has associated documents
			log.warn("Hard delete failed for document type {} due to existing documents, falling back to soft delete", id, e);
			documentType.setStatus(Status.DELETED);
			repository.save(documentType);
		}
	}

	@Override
	@Transactional
	public DocumentTypeDTO activateDocumentType(UUID id) {
		log.info("Activating document type with ID: {}", id);

		DocumentType documentType = findByIdAndStatusNot(id, Status.DELETED);

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
	public DocumentTypeDTO deactivateDocumentType(UUID id) {
		log.info("Deactivating document type with ID: {}", id);

		DocumentType documentType = findByIdAndStatusNot(id, Status.DELETED);

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
	public DocumentTypeDTO restoreDocumentType(UUID id) {
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
	public void validateDocumentTypeRequest(CreateDocumentTypeRequestDTO payload) {
		if (payload.getCode() == null || payload.getCode().trim().isEmpty()) {
			throw CustomException.badRequest("documents.errors.type-code-required");
		}

		if (payload.getName() == null || payload.getName().trim().isEmpty()) {
			throw CustomException.badRequest("documents.errors.type-name-required");
		}
	}
}