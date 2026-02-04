package org.openhospital.smartdoc.modules.persons.port;

import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@HttpExchange("/persons")
public interface IPersonService {

	@GetExchange
	Page<PersonDTO> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@GetExchange
	Page<PersonDTO> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean includeInactive, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@PostExchange
	PersonDTO createPerson(@RequestBody CreatePersonRequestDTO payload);

	@GetExchange("/{id}")
	PersonDTO findPersonById(@PathVariable UUID id);

	@PutExchange("/{id}")
	PersonDTO updatePerson(@PathVariable UUID id, @RequestBody UpdatePersonRequestDTO payload);

	@PatchExchange("/{id}")
	PersonDTO patchPerson(@PathVariable UUID id, @RequestBody PatchPersonRequestDTO payload);

	@DeleteExchange("/{id}")
	void deletePerson(@PathVariable UUID id);

	@PutExchange("/{id}/activate")
	PersonDTO activatePerson(@PathVariable UUID id);

	@PutExchange("/{id}/deactivate")
	PersonDTO deactivatePerson(@PathVariable UUID id);

	@PostExchange("/{id}/restore")
	PersonDTO restorePerson(@PathVariable UUID id);

	@GetExchange("/{id}/documents")
	Page<DocumentDTO> findPersonDocuments(@PathVariable UUID id, @RequestParam(required = false) UUID type, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);
}
