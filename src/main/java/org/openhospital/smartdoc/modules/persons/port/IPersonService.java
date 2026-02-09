package org.openhospital.smartdoc.modules.persons.port;

import org.openhospital.smartdoc.openapi.*;
import org.openhospital.smartdoc.types.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

import java.time.Instant;
import java.util.UUID;

@HttpExchange("/persons")
public interface IPersonService {

	@GetExchange
	Page<PersonResponse> findPersons(@RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean includeInactive, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@PostExchange
	PersonResponse createPerson(@RequestBody CreatePersonRequest payload);

	@GetExchange("/{id}")
	PersonResponse findPersonById(@PathVariable UUID id);

	@PutExchange("/{id}")
	PersonResponse updatePerson(@PathVariable UUID id, @RequestBody UpdatePersonRequest payload);

	@PatchExchange("/{id}")
	PersonResponse patchPerson(@PathVariable UUID id, @RequestBody PatchPersonRequest payload);

	@DeleteExchange("/{id}")
	void deletePerson(@PathVariable UUID id);

	@PutExchange("/{id}/activate")
	PersonResponse activatePerson(@PathVariable UUID id);

	@PutExchange("/{id}/deactivate")
	PersonResponse deactivatePerson(@PathVariable UUID id);

	@PostExchange("/{id}/restore")
	PersonResponse restorePerson(@PathVariable UUID id);

	@GetExchange("/{id}/documents")
	Page<DocumentResponse> findPersonDocuments(@PathVariable UUID id, @RequestParam(required = false) String type, @RequestParam(required = false) Instant fromDate, @RequestParam(required = false) Instant toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);
}
