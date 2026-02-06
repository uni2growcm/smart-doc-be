package org.openhospital.smartdoc.modules.persons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.mapper.DocumentMapper;
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
@Service(PersonService.NAME)
@RequiredArgsConstructor
public class PersonService implements IPersonService {

	public static final String NAME = "PersonService";

	private final PersonRepository repository;
	private final PersonMapper mapper;
	private final DocumentRepository documentRepository;
	private final DocumentMapper documentMapper;

	private Person findById(UUID id) {
		return repository.findById(id).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));
	}

	private Person findNotDeletedById(UUID id) {
		return repository.findByIdAndStatusNot(id, Status.DELETED).orElseThrow(() -> CustomException.notFound("persons.errors.not-found", new Object[]{id}));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PersonResponse> findPersons(String name, boolean includeInactive, int page, int size) {
		log.debug("Finding persons with name filter: {}, includeInactive: {}, page: {}, size: {}", name, includeInactive, page, size);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

			List<Status> statuses = includeInactive ? List.of(Status.ACTIVE, Status.INACTIVE) : List.of(Status.ACTIVE);

			var personPage = StringUtils.hasText(name) ? repository.findByNameContainingIgnoreCaseAndStatusIn(name.trim(), statuses, pageable) : repository.findByStatusIn(statuses, pageable);

			Page<PersonResponse> result = Page.from(personPage, mapper::toDtos);

			log.info("Found {} persons (page {}/{}, total: {})", result.getData().size(), page, personPage.getTotalPages(), personPage.getTotalElements());

			return result;
		} catch (Exception e) {
			log.error("Failed to find persons", e);
			throw CustomException.internal("persons.errors.search-failed");
		}
	}

	@Override
	@Transactional
	public PersonResponse createPerson(CreatePersonRequest payload) {
		log.info("Creating person with PID: {}", payload.getPid());

		validatePersonRequest(payload);

		Person entity = mapper.toModel(payload);
		validatePerson(entity);
		Person saved = repository.save(entity);
		PersonResponse result = mapper.toDto(saved);

		log.info("Person created successfully with ID: {}", saved.getId());
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public PersonResponse findPersonById(UUID id) {
		log.debug("Finding person by ID: {}", id);

		Person entity = findNotDeletedById(id);

		PersonResponse result = mapper.toDto(entity);
		log.debug("Person found: {} ({})", entity.getName(), entity.getPid());
		return result;
	}

	@Override
	@Transactional
	public PersonResponse updatePerson(UUID id, UpdatePersonRequest payload) {
		log.info("Updating person with ID: {}", id);

		Person existing = findNotDeletedById(id);

		mapper.updateModel(payload, existing);
		log.info("Versions : new{} old({})", payload.getVersion(), existing.getVersion());
		Person saved = repository.saveAndFlush(existing);
		PersonResponse result = mapper.toDto(saved);

		log.info("Person updated successfully: {} ({})", saved.getName(), saved.getPid());
		return result;
	}

	@Override
	@Transactional
	public PersonResponse patchPerson(UUID id, PatchPersonRequest payload) {
		log.info("Patching person with ID: {}", id);

		Person existing = findNotDeletedById(id);

		mapper.patchModel(payload, existing);
		Person saved = repository.save(existing);
		PersonResponse result = mapper.toDto(saved);

		log.info("Person patched successfully: {} ({})", saved.getName(), saved.getPid());
		return result;
	}

	@Transactional
	public void deletePerson(UUID id) {
		log.info("Deleting person with ID: {}", id);

		Person person = findNotDeletedById(id);

		try {
			// Try hard delete first
			repository.deleteById(id);
			log.info("Person {} hard DELETED successfully", id);
		} catch (DataIntegrityViolationException e) {
			// Foreign key constraint violation - person has associated documents
			log.warn("Hard delete failed for person {} due to existing documents, falling back to soft delete", id, e);
			person.setStatus(Status.DELETED);
			repository.save(person);
		}
	}

	@Override
	@Transactional
	public PersonResponse activatePerson(UUID id) {
		log.info("Activating person with ID: {}", id);

		Person person = findNotDeletedById(id);

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
	public PersonResponse deactivatePerson(UUID id) {
		log.info("Deactivating person with ID: {}", id);

		Person person = findNotDeletedById(id);

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
	public PersonResponse restorePerson(UUID id) {
		log.info("Restoring person with ID: {}", id);

		Person person = findById(id);

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
	public Page<DocumentResponse> findPersonDocuments(UUID id, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		log.debug("Finding documents for person ID: {}, type: {}, date range: {} to {}, page: {}, size: {}", id, type, fromDate, toDate, page, size);

		// Verify person exists and is active
		validatePersonReference(id);

		Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

		// Convert LocalDate to Instant for database query
		Instant fromInstant = fromDate != null ? fromDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC) : null;
		Instant toInstant = toDate != null ? toDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC) : null;

		org.springframework.data.domain.Page<Document> documentPage = documentRepository.findWithFilters(id, type, fromInstant, toInstant, pageable);

		Page<DocumentResponse> result = Page.from(documentPage, documentMapper::toDtos);

		log.info("Found {} documents for person {} (page {}/{}, total: {})", result.getData().size(), id, page, documentPage.getTotalPages(), documentPage.getTotalElements());

		return result;
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
	}

	/**
	 * Validates person reference for operations.
	 */
	public void validatePersonReference(UUID personId) {
		Person person = findNotDeletedById(personId);

		if (person.getStatus() != Status.ACTIVE) {
			throw CustomException.badRequest("persons.errors.not-active");
		}
	}

	/**
	 * Validates the create request payload.
	 */
	public void validatePersonRequest(CreatePersonRequest payload) {
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