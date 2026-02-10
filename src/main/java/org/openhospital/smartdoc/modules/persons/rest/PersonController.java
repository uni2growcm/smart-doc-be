package org.openhospital.smartdoc.modules.persons.rest;

import org.openhospital.smartdoc.modules.persons.port.IPersonService;
import org.openhospital.smartdoc.modules.persons.service.PersonService;
import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class PersonController implements IPersonService {
	private final IPersonService service;

	public PersonController(@Qualifier(PersonService.NAME) IPersonService service) {
		this.service = service;
	}

	@Override
	public Page<PersonResponse> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean includeInactive, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findPersons(name, includeInactive, page, size);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public PersonResponse createPerson(@RequestBody CreatePersonRequest payload) {
		return service.createPerson(payload);
	}

	@Override
	public PersonResponse findPersonById(@PathVariable UUID id) {
		return service.findPersonById(id);
	}

	@Override
	public PersonResponse updatePerson(@PathVariable UUID id, @RequestBody UpdatePersonRequest payload) {
		return service.updatePerson(id, payload);
	}

	@Override
	public PersonResponse patchPerson(@PathVariable UUID id, @RequestBody PatchPersonRequest payload) {
		return service.patchPerson(id, payload);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePerson(@PathVariable UUID id) {
		service.deletePerson(id);
	}

	@Override
	public PersonResponse activatePerson(@PathVariable UUID id) {
		return service.activatePerson(id);
	}

	@Override
	public PersonResponse deactivatePerson(@PathVariable UUID id) {
		return service.deactivatePerson(id);
	}

	@Override
	public PersonResponse restorePerson(@PathVariable UUID id) {
		return service.restorePerson(id);
	}

	@Override
	public Page<DocumentResponse> findPersonDocuments(@PathVariable UUID id, @RequestParam(required = false) String type, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findPersonDocuments(id, type, fromDate, toDate, page, size);
	}
}
