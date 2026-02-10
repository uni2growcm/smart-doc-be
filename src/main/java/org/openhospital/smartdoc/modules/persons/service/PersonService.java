package org.openhospital.smartdoc.modules.persons.service;

import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.service.DocumentService;
import org.openhospital.smartdoc.modules.persons.mapper.PersonMapper;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.openhospital.smartdoc.modules.persons.repository.PersonRepository;
import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for person operations.
 * Provides CRUD operations and document relationships management.
 */
@Slf4j
@Service(PersonService.NAME)
public class PersonService implements IPersonService {

	public static final String NAME = "PersonService";

	private final PersonRepository repository;
	private final PersonMapper mapper;
	private final IDocumentService documentService;

	PersonService(PersonRepository repository, PersonMapper mapper, @Qualifier(DocumentService.NAME) IDocumentService documentService) {
		this.repository = repository;
		this.mapper = mapper;
		this.documentService = documentService;
	}

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
		Person saved = repository.save(existing);
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

		person.setStatus(Status.DELETED);
		repository.save(person);

		log.info("Person {} soft deleted successfully", id);
	}

	@Override
	@Transactional
	public PersonResponse activatePerson(UUID id) {
		log.info("Activating person with ID: {}", id);

		Person person = findNotDeletedById(id);

		if (person.getStatus() == Status.ACTIVE) {
			throw CustomException.conflict("persons.errors.already-active");
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
			throw CustomException.conflict("persons.errors.already-inactive");
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
			throw CustomException.conflict("persons.errors.not-deleted");
		}

		person.setStatus(Status.ACTIVE);
		Person saved = repository.save(person);
		log.info("Person {} restored and reactivated successfully", id);
		return mapper.toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentResponse> findPersonDocuments(UUID id, String type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		var person = findById(id);
		log.debug("Finding documents for person ID: {}, type: {}, date range: {} to {}, page: {}, size: {}", person.getPid(), type, fromDate, toDate, page, size);

		return documentService.findDocuments(person.getPid(), type, fromDate, toDate, page, size);
	}

	/**
	 * Validates person entity with business rules.
	 */
	public void validatePerson(Person person) {
		// PID uniqueness check (excluding DELETED)
		if (person.getId() == null && person.getPid() > 0) {
			if (repository.existsByPidAndStatusNot(person.getPid(), Status.DELETED)) {
				throw CustomException.conflict("persons.errors.pid-already-exists");
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
	public void validatePersonReference(int personId) {
		if (!repository.existsByPidAndStatusNot(personId, Status.DELETED)) {
			throw CustomException.notFound("persons.errors.not-found", new Object[]{personId});
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