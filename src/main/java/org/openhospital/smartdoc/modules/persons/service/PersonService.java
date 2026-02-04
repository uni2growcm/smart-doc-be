package org.openhospital.smartdoc.modules.persons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.modules.documents.repository.DocumentRepository;
import org.openhospital.smartdoc.modules.persons.mapper.PersonMapper;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.openhospital.smartdoc.modules.persons.repository.PersonRepository;
import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for person operations.
 * Provides CRUD operations and document relationships management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService implements IPersonService {

	private final PersonRepository repository;
	private final PersonMapper mapper;
	private final DocumentRepository documentRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<PersonDTO> findPersons(String name, int page, int size) {
		log.debug("Finding persons with name filter: {}, page: {}, size: {}", name, page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

			var personPage = StringUtils.hasText(name) ? repository.findByNameContainingIgnoreCase(name.trim(), pageable) : repository.findAll(pageable);


			Page<PersonDTO> result = Page.from(personPage, mapper::toDtos);

			log.info("Found {} persons (page {}/{}, total: {})",
				result.getData().size(), page, personPage.getTotalPages(), personPage.getTotalElements());

			return result;
		} catch (Exception e) {
			log.error("Failed to find persons", e);
			throw CustomException.internal("persons.errors.search-failed");
		}
	}

	@Override
	@Transactional
	public PersonDTO createPerson(CreatePersonRequestDTO payload) {
		log.info("Creating person with PID: {}", payload.getPid());

		validateCreateRequest(payload);

		try {
			Person entity = mapper.toModel(payload);
			Person saved = repository.save(entity);
			PersonDTO result = mapper.toDto(saved);

			log.info("Person created successfully with ID: {}", saved.getId());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Person creation failed due to data integrity violation: {}", e.getMessage());
			if (e.getMessage().contains("uk_persons_pid")) {
				throw CustomException.badRequest("persons.errors.pid-already-exists", new Object[]{payload.getPid()});
			}
			throw CustomException.badRequest("persons.errors.creation-constraint-violation");
		} catch (Exception e) {
			log.error("Failed to create person", e);
			throw CustomException.internal("persons.errors.creation-failed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PersonDTO findPersonById(UUID id) {
		log.debug("Finding person by ID: {}", id);

		Person entity = repository.findById(id)
		                          .orElseThrow(() -> {
			                          log.warn("Person not found with ID: {}", id);
			                          return CustomException.notFound("persons.errors.not-found", new Object[]{id});
			                          });

		PersonDTO result = mapper.toDto(entity);
		log.debug("Person found: {} ({})", entity.getName(), entity.getPid());
		return result;
	}

	@Override
	@Transactional
	public PersonDTO updatePerson(UUID id, UpdatePersonRequestDTO payload) {
		log.info("Updating person with ID: {}", id);

		Person existing = repository.findById(id)
		                            .orElseThrow(() -> {
			                            log.warn("Person not found for update with ID: {}", id);
			                            return CustomException.notFound("persons.errors.not-found", new Object[]{id});
			                            });

		try {
			mapper.updateModel(payload, existing);
			Person saved = repository.save(existing);
			PersonDTO result = mapper.toDto(saved);

			log.info("Person updated successfully: {} ({})", saved.getName(), saved.getPid());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Person update failed due to data integrity violation");
			throw CustomException.badRequest("persons.errors.update-constraint-violation");
		} catch (Exception e) {
			log.error("Failed to update person", e);
			throw CustomException.internal("persons.errors.update-failed");
		}
	}

	@Override
	@Transactional
	public PersonDTO patchPerson(UUID id, PatchPersonRequestDTO payload) {
		log.info("Patching person with ID: {}", id);

		Person existing = repository.findById(id)
		                            .orElseThrow(() -> {
			                            log.warn("Person not found for patch with ID: {}", id);
			                            return CustomException.notFound("persons.errors.not-found", new Object[]{id});
			                            });

		try {
			mapper.patchModel(payload, existing);
			Person saved = repository.save(existing);
			PersonDTO result = mapper.toDto(saved);

			log.info("Person patched successfully: {} ({})", saved.getName(), saved.getPid());
			return result;
		} catch (DataIntegrityViolationException e) {
			log.warn("Person patch failed due to data integrity violation");
			throw CustomException.badRequest("persons.errors.patch-constraint-violation");
		} catch (Exception e) {
			log.error("Failed to patch person", e);
			throw CustomException.internal("persons.errors.patch-failed");
		}
	}

	@Override
	@Transactional
	public void deletePerson(UUID id) {
		log.info("Deleting person with ID: {}", id);

		Person existing = repository.findById(id)
		                            .orElseThrow(() -> {
			                            log.warn("Person not found for deletion with ID: {}", id);
			                            return CustomException.notFound("persons.errors.not-found", new Object[]{id});
			                            });

		try {
			repository.deleteById(id);
			log.info("Person deleted successfully: {} ({})", existing.getName(), existing.getPid());
		} catch (Exception e) {
			log.error("Failed to delete person", e);
			throw CustomException.internal("persons.errors.deletion-failed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedDocumentDTO findPersonDocuments(
		UUID id, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size
	                                               ) {
		log.debug("Finding documents for person ID: {}, type: {}, date range: {} to {}, page: {}, size: {}",
			id, type, fromDate, toDate, page, size);

		// Verify person exists
		if (!repository.existsById(id)) {
			throw CustomException.notFound("persons.errors.not-found", new Object[]{id});
		}

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

			// Convert LocalDate to Instant for database query
			Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
			Instant toInstant = toDate != null ? toDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC) : null;

			org.springframework.data.domain.Page<Document> documentPage = documentRepository.findWithFilters(
				id, type, fromInstant, toInstant, pageable);

			List<DocumentDTO> content = documentPage.getContent().stream()
			                                        .map(doc -> {
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
			                                        })
			                                        .toList();

			PageInfoDTO pageInfo = new PageInfoDTO(
				page,
				size,
				(int) documentPage.getTotalElements(),
				documentPage.getTotalPages()
			);

			PaginatedDocumentDTO result = new PaginatedDocumentDTO();
			result.setData(content);
			result.setMetadata(pageInfo);

			log.info("Found {} documents for person {} (page {}/{}, total: {})",
				content.size(), id, page, documentPage.getTotalPages(), documentPage.getTotalElements());

			return result;
		} catch (CustomException e) {
			throw e; // Re-throw custom exceptions
		} catch (Exception e) {
			log.error("Failed to find person documents", e);
			throw CustomException.internal("persons.errors.documents-search-failed");
		}
	}

	/**
	 * Validates the create request payload.
	 */
	private void validateCreateRequest(CreatePersonRequestDTO payload) {
		if (payload.getPid() == null || payload.getPid().trim().isEmpty()) {
			throw CustomException.badRequest("persons.errors.pid-required");
		}

		if (payload.getName() == null || payload.getName().trim().isEmpty()) {
			throw CustomException.badRequest("persons.errors.name-required");
		}

		// Additional validation can be added here
		if (payload.getEmail() != null && !payload.getEmail().contains("@")) {
			throw CustomException.badRequest("persons.errors.email-invalid");
		}
	}
}