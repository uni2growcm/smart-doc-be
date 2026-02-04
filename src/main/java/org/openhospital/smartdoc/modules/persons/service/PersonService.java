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
		return findPersons(name, false, page, size);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PersonDTO> findPersons(String name, boolean includeInactive, int page, int size) {
		log.debug("Finding persons with name filter: {}, includeInactive: {}, page: {}, size: {}", name, includeInactive, page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

			List<Status> statuses = includeInactive ? List.of(Status.ACTIVE, Status.INACTIVE) : List.of(Status.ACTIVE);

			var personPage = StringUtils.hasText(name) ? repository.findByNameContainingIgnoreCaseAndStatusIn(name.trim(), statuses, pageable) : repository.findByStatusIn(statuses, pageable);

			Page<PersonDTO> result = Page.from(personPage, persons -> persons.stream().map(mapper::toDto).toList());

			log.info("Found {} persons (page {}/{}, total: {})", result.getData().size(), page, personPage.getTotalPages(), personPage.getTotalElements());

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

		validatePersonRequest(payload);

		try {
			Person entity = mapper.toModel(payload);
			validatePerson(entity);
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

		Person entity = repository.findById(id).orElseThrow(() -> {
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

		Person existing = repository.findById(id).orElseThrow(() -> {
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

		Person existing = repository.findById(id).orElseThrow(() -> {
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

		Person person = repository.findById(id).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));

		try {
			// Try hard delete first
			repository.deleteById(id);
			log.info("Person {} hard DELETED successfully", id);
		} catch (DataIntegrityViolationException e) {
			// Foreign key constraint violation - person has associated documents
			log.warn("Hard delete failed for person {} due to existing documents, falling back to soft delete", id, e);
			person.setStatus(Status.DELETED);
			repository.save(person);
			log.info("Person {} soft DELETED due to dependencies", id);
			throw CustomException.badRequest("persons.errors.has-dependencies");
		}
	}

	@Override
	@Transactional
	public PersonDTO activatePerson(UUID id) {
		log.info("Activating person with ID: {}", id);

		Person person = repository.findById(id).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));

		if (person.getStatus() == Status.ACTIVE) {
			throw CustomException.badRequest("persons.errors.already-active");
		}

		person.setStatus(Status.ACTIVE);
		Person saved = repository.save(person);
		log.info("Person {} activated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional
	public PersonDTO deactivatePerson(UUID id) {
		log.info("Deactivating person with ID: {}", id);

		Person person = repository.findById(id).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));

		if (person.getStatus() == Status.INACTIVE) {
			throw CustomException.badRequest("persons.errors.already-inactive");
		}

		person.setStatus(Status.INACTIVE);
		Person saved = repository.save(person);
		log.info("Person {} deactivated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional
	public PersonDTO restorePerson(UUID id) {
		log.info("Restoring person with ID: {}", id);

		Person person = repository.findById(id).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));

		if (person.getStatus() != Status.DELETED) {
			throw CustomException.badRequest("persons.errors.not-DELETED");
		}

		person.setStatus(Status.ACTIVE);
		Person saved = repository.save(person);
		log.info("Person {} restored and reactivated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedDocumentDTO findPersonDocuments(UUID id, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		log.debug("Finding documents for person ID: {}, type: {}, date range: {} to {}, page: {}, size: {}", id, type, fromDate, toDate, page, size);

		// Verify person exists and is active
		validatePersonReference(id);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

			// Convert LocalDate to Instant for database query
			Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
			Instant toInstant = toDate != null ? toDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC) : null;

			org.springframework.data.domain.Page<Document> documentPage = documentRepository.findWithFilters(id, type, fromInstant, toInstant, pageable);

			List<DocumentDTO> content = documentPage.getContent().stream().map(doc -> {
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
			}).toList();

			PageInfoDTO pageInfo = new PageInfoDTO(page, size, (int) documentPage.getTotalElements(), documentPage.getTotalPages());

			PaginatedDocumentDTO result = new PaginatedDocumentDTO();
			result.setData(content);
			result.setMetadata(pageInfo);

			log.info("Found {} documents for person {} (page {}/{}, total: {})", content.size(), id, page, documentPage.getTotalPages(), documentPage.getTotalElements());

			return result;
		} catch (CustomException e) {
			throw e; // Re-throw custom exceptions
		}
	}

	/**
	 * Validates person entity with business rules.
	 */
	public void validatePerson(Person person) {
		// PID uniqueness check (excluding DELETED)
		if (person.getId() == null && person.getPid() != null) {
			if (repository.existsByPidAndStatusNot(person.getPid(), Status.DELETED)) {
				throw CustomException.badRequest("persons.errors.pid-already-exists");
			}
		}

		// Business rules
		if (person.getName() == null || person.getName().trim().length() < 2) {
			throw CustomException.badRequest("persons.errors.name-too-short");
		}

		// Email validation if provided
		if (person.getEmail() != null && !person.getEmail().contains("@")) {
			throw CustomException.badRequest("persons.errors.email-invalid");
		}

		// Age validation if birth date provided (assuming we add birthDate field later)
		// Additional business rules can be added here
	}

	/**
	 * Validates person reference for operations.
	 */
	public void validatePersonReference(UUID personId) {
		Person person = repository.findByIdAndStatusNot(personId, Status.DELETED).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{personId}));

		if (person.getStatus() != Status.ACTIVE) {
			throw CustomException.badRequest("persons.errors.not-active");
		}
	}

	/**
	 * Validates the create request payload.
	 */
	public void validatePersonRequest(CreatePersonRequestDTO payload) {
		if (payload.getPid() == null || payload.getPid().trim().isEmpty()) {
			throw CustomException.badRequest("persons.errors.pid-required");
		}

		if (payload.getName() == null || payload.getName().trim().isEmpty()) {
			throw CustomException.badRequest("persons.errors.name-required");
		}

		// Email validation
		if (payload.getEmail() != null && !payload.getEmail().contains("@")) {
			throw CustomException.badRequest("persons.errors.email-invalid");
		}
	}


}