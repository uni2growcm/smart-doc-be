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
        log.debug("Fetching all document types");

        try {
            List<DocumentType> documentTypes = (List<DocumentType>) repository.findAll();
            List<DocumentTypeDTO> dtos = documentTypes.stream()
                .map(mapper::toDto)
                .toList();

            log.info("Retrieved {} document types", dtos.size());
            return dtos;
        } catch (Exception e) {
            log.error("Failed to retrieve document types", e);
            throw CustomException.internal("document.types.fetch.failed");
        }
    }

    @Override
    @Transactional
    public DocumentTypeDTO createDocumentType(CreateDocumentTypeRequestDTO payload) {
        log.info("Creating document type with code: {}", payload.getCode());

        validateCreateRequest(payload);

        try {
            DocumentType entity = mapper.toModel(payload);
            DocumentType saved = repository.save(entity);
            DocumentTypeDTO result = mapper.toDto(saved);

            log.info("Document type created successfully with ID: {}", saved.getId());
            return result;
        } catch (DataIntegrityViolationException e) {
            log.warn("Document type creation failed due to data integrity violation: {}", e.getMessage());
            throw CustomException.badRequest("document.type.code.already.exists", new Object[]{payload.getCode()});
        } catch (Exception e) {
            log.error("Failed to create document type", e);
            throw CustomException.internal("document.type.creation.failed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeDTO findDocumentTypeById(UUID id) {
        log.debug("Finding document type by ID: {}", id);

        DocumentType entity = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Document type not found with ID: {}", id);
                return CustomException.notFound("document.type.not.found", new Object[]{id});
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
                return CustomException.notFound("document.type.not.found", new Object[]{id});
            });

        try {
            mapper.updateModel(payload, existing);
            DocumentType saved = repository.save(existing);
            DocumentTypeDTO result = mapper.toDto(saved);

            log.info("Document type updated successfully: {}", saved.getCode());
            return result;
        } catch (DataIntegrityViolationException e) {
            log.warn("Document type update failed due to data integrity violation");
            throw CustomException.badRequest("document.type.code.already.exists");
        } catch (Exception e) {
            log.error("Failed to update document type", e);
            throw CustomException.internal("document.type.update.failed");
        }
    }

    @Override
    @Transactional
    public DocumentTypeDTO patchDocumentType(UUID id, PatchDocumentTypeRequestDTO payload) {
        log.info("Patching document type with ID: {}", id);

        DocumentType existing = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Document type not found for patch with ID: {}", id);
                return CustomException.notFound("document.type.not.found", new Object[]{id});
            });

        try {
            mapper.patchModel(payload, existing);
            DocumentType saved = repository.save(existing);
            DocumentTypeDTO result = mapper.toDto(saved);

            log.info("Document type patched successfully: {}", saved.getCode());
            return result;
        } catch (DataIntegrityViolationException e) {
            log.warn("Document type patch failed due to data integrity violation");
            throw CustomException.badRequest("document.type.code.already.exists");
        } catch (Exception e) {
            log.error("Failed to patch document type", e);
            throw CustomException.internal("document.type.patch.failed");
        }
    }

    @Override
    @Transactional
    public void deleteDocumentType(UUID id) {
        log.info("Deleting document type with ID: {}", id);

        DocumentType existing = repository.findById(id)
            .orElseThrow(() -> {
                log.warn("Document type not found for deletion with ID: {}", id);
                return CustomException.notFound("document.type.not.found", new Object[]{id});
            });

        try {
            repository.deleteById(id);
            log.info("Document type deleted successfully: {}", existing.getCode());
        } catch (Exception e) {
            log.error("Failed to delete document type", e);
            throw CustomException.internal("document.type.deletion.failed");
        }
    }

    /**
     * Validates the create request payload.
     */
    private void validateCreateRequest(CreateDocumentTypeRequestDTO payload) {
        if (payload.getCode() == null || payload.getCode().trim().isEmpty()) {
            throw CustomException.badRequest("document.type.code.required");
        }

        if (payload.getName() == null || payload.getName().trim().isEmpty()) {
            throw CustomException.badRequest("document.type.name.required");
        }

        // Check if code already exists
        if (repository.findByCode(payload.getCode()).isPresent()) {
            throw CustomException.badRequest("document.type.code.already.exists", new Object[]{payload.getCode()});
        }
    }
}