package org.openhospital.smartdoc.modules.persons.port;

import java.time.LocalDate;
import java.util.UUID;
import org.openhospital.smartdoc.openapi.CreatePersonRequestDTO;
import org.openhospital.smartdoc.openapi.PaginatedDocumentDTO;
import org.openhospital.smartdoc.openapi.PaginatedPersonDTO;
import org.openhospital.smartdoc.openapi.PatchPersonRequestDTO;
import org.openhospital.smartdoc.openapi.PersonDTO;
import org.openhospital.smartdoc.openapi.UpdatePersonRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/persons")
public interface IPersonService {

    @GetExchange
    PaginatedPersonDTO findPersons(@RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

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

    @GetExchange("/{id}/documents")
    PaginatedDocumentDTO findPersonDocuments(@PathVariable UUID id, @RequestParam(required = false) UUID type,
        @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);
}
