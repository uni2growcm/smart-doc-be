package org.openhospital.smartdoc.modules.persons.rest;

import lombok.RequiredArgsConstructor;
import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.openhospital.smartdoc.modules.persons.service.PersonService;
import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PersonController implements IPersonService {
	private final PersonService service;

	@Override
	public Page<PersonDTO> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findPersons(name, page, size);
	}

	@Override
	public Page<PersonDTO> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean includeInactive, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findPersons(name, includeInactive, page, size);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public PersonDTO createPerson(@RequestBody CreatePersonRequestDTO payload) {
		return service.createPerson(payload);
	}

	@Override
	public PersonDTO findPersonById(@PathVariable UUID id) {
		return service.findPersonById(id);
	}

	@Override
	public PersonDTO updatePerson(@PathVariable UUID id, @RequestBody UpdatePersonRequestDTO payload) {
		return service.updatePerson(id, payload);
	}

	@Override
	public PersonDTO patchPerson(@PathVariable UUID id, @RequestBody PatchPersonRequestDTO payload) {
		return service.patchPerson(id, payload);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePerson(@PathVariable UUID id) {
		service.deletePerson(id);
	}

	@Override
	public PersonDTO activatePerson(@PathVariable UUID id) {
		return service.activatePerson(id);
	}

	@Override
	public PersonDTO deactivatePerson(@PathVariable UUID id) {
		return service.deactivatePerson(id);
	}

	@Override
	public PersonDTO restorePerson(@PathVariable UUID id) {
		return service.restorePerson(id);
	}

	@Override
	public Page<DocumentDTO> findPersonDocuments(@PathVariable UUID id, @RequestParam(required = false) UUID type, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findPersonDocuments(id, type, fromDate, toDate, page, size);
	}
}
