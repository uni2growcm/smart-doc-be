package org.openhospital.smartdoc.modules.persons.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.modules.persons.mapper.PersonMapper;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.openhospital.smartdoc.modules.persons.repository.PersonRepository;
import org.openhospital.smartdoc.openapi.*;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class PersonService implements IPersonService {
	final PersonRepository repository;
	final PersonMapper mapper;

	@Override
	public PaginatedPersonDTO findPersons(String name, int page, int size) {
		return null;
	}

	@Override
	public PersonDTO createPerson(CreatePersonRequestDTO payload) {
		return null;
	}

	@Override
	public PersonDTO findPersonById(UUID id) {
		return null;
	}

	@Override
	public PersonDTO updatePerson(UUID id, UpdatePersonRequestDTO payload) {
		return null;
	}

	@Override
	public PersonDTO patchPerson(UUID id, PatchPersonRequestDTO payload) {
		return null;
	}

	@Override
	public void deletePerson(UUID id) {

	}

	@Override
	public PaginatedDocumentDTO findPersonDocuments(
		UUID id, UUID type, LocalDate fromDate, LocalDate toDate, int page,
		int size
	                                               ) {
		return null;
	}
}
