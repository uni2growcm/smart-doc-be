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
@Service
@RequiredArgsConstructor
public class DocumentTypeService implements IDocumentTypeService {

	private final DocumentTypeRepository repository;
	private final DocumentTypeMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTypeDTO> getDocumentTypes() {
		return getDocumentTypes(false);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentTypeDTO> getDocumentTypes(boolean includeInactive) {
		log.debug("Fetching all document types, includeInactive: {}", includeInactive);

		try {
			List<Status> statuses = includeInactive
				? List.of(Status.ACTIVE, Status.INACTIVE)
				: List.of(Status.ACTIVE);

			Page<DocumentType> documentTypePage = repository.findByStatusIn(statuses, Pageable.unpaged());
			List<DocumentType> documentTypes = documentTypePage.getContent();
			List<DocumentTypeDTO> dtos = documentTypes.stream()
			                                          .map(mapper::toDto)
			                                          .toList();

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

		try {
			DocumentType entity = mapper.toModel(payload);
			validateDocumentType(entity);  // Entity validation with business rules
			DocumentType saved = repository.save(entity);
			DocumentTypeDTO result = mapper.toDto(saved);

			log.info("Document type created successfully with ID: {}", saved.getId());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Document type creation failed due to data integrity violation: {}", e.getMessage());
			throw CustomException.badRequest("documents.errors.type-code-already-exists", new Object[]{payload.getCode()});
		} catch (Exception e) {
			log.error("Failed to create document type", e);
			throw CustomException.internal("documents.errors.type-creation-failed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentTypeDTO findDocumentTypeById(UUID id) {
		log.debug("Finding document type by ID: {}", id);

		DocumentType entity = repository.findById(id)
		                                .orElseThrow(() -> {
			                                log.warn("Document type not found with ID: {}", id);
			                                return CustomException.notFound("documents.errors.type-not-found", new Object[]{id});
		                                });

		DocumentTypeDTO result = mapper.toDto(entity);
		log.debug("Document type found: {}", entity.getCode());
		return result;
	}

	@Override
	@Transactional
	public DocumentTypeDTO updateDocumentType(UUID id, UpdateDocumentTypeRequestDTO payload) {
		log.info("Updating document type with ID: {}", id);

		DocumentType existing = repository.findById(id)
		                                  .orElseThrow(() -> {
			                                  log.warn("Document type not found for update with ID: {}", id);
			                                  return CustomException.notFound("documents.errors.type-not-found", new Object[]{id});
		                                  });

		try {
			mapper.updateModel(payload, existing);
			DocumentType saved = repository.save(existing);
			DocumentTypeDTO result = mapper.toDto(saved);

			log.info("Document type updated successfully: {}", saved.getCode());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Document type update failed due to data integrity violation");
			throw CustomException.badRequest("documents.errors.type-code-already-exists");
		} catch (Exception e) {
			log.error("Failed to update document type", e);
			throw CustomException.internal("documents.errors.type-update-failed");
		}
	}

	@Override
	@Transactional
	public DocumentTypeDTO patchDocumentType(UUID id, PatchDocumentTypeRequestDTO payload) {
		log.info("Patching document type with ID: {}", id);

		DocumentType existing = repository.findById(id)
		                                  .orElseThrow(() -> {
			                                  log.warn("Document type not found for patch with ID: {}", id);
			                                  return CustomException.notFound("documents.errors.type-not-found", new Object[]{id});
		                                  });

		try {
			mapper.patchModel(payload, existing);
			DocumentType saved = repository.save(existing);
			DocumentTypeDTO result = mapper.toDto(saved);

			log.info("Document type patched successfully: {}", saved.getCode());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Document type patch failed due to data integrity violation");
			throw CustomException.badRequest("documents.errors.type-code-already-exists");
		} catch (Exception e) {
			log.error("Failed to patch document type", e);
			throw CustomException.internal("documents.errors.type-patch-failed");
		}
	}

	@Override
	@Transactional
	public void deleteDocumentType(UUID id) {
		log.info("Deleting document type with ID: {}", id);

		DocumentType documentType = repository.findById(id).orElseThrow(
			() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id})
		                                                               );

		try {
			// Try hard delete first
			repository.deleteById(id);
			log.info("Document type {} hard deleted successfully", id);
		} catch (DataIntegrityViolationException e) {
			// Foreign key constraint violation - document type has associated documents
			log.warn("Hard delete failed for document type {} due to existing documents, falling back to soft delete", id, e);
			documentType.setStatus(Status.DELETED);
			repository.save(documentType);
			log.info("Document type {} soft deleted due to dependencies", id);
			throw CustomException.badRequest("documents.errors.type-has-dependencies");
		}
	}

	@Override
	@Transactional
	public DocumentTypeDTO activateDocumentType(UUID id) {
		log.info("Activating document type with ID: {}", id);

		DocumentType documentType = repository.findById(id).orElseThrow(
			() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id})
		                                                               );

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

		DocumentType documentType = repository.findById(id).orElseThrow(
			() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id})
		                                                               );

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

		DocumentType documentType = repository.findById(id).orElseThrow(
			() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{id})
		                                                               );

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
	 * Validates document type reference for operations.
	 */
	public void validateDocumentTypeReference(UUID typeId) {
		DocumentType documentType = repository.findByIdAndStatusNot(typeId, Status.DELETED)
		                                      .orElseThrow(() -> CustomException.notFound("documents.errors.type-not-found", new Object[]{typeId}));

		if (documentType.getStatus() != Status.ACTIVE) {
			throw CustomException.badRequest("documents.errors.type-not-active");
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