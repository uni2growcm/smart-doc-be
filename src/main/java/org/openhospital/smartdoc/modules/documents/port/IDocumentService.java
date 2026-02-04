package org.openhospital.smartdoc.modules.documents.port;

import java.time.LocalDate;
import java.util.UUID;
import org.openhospital.smartdoc.openapi.DocumentDTO;
import org.openhospital.smartdoc.openapi.PaginatedDocumentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/documents")
public interface IDocumentService {

    @GetExchange
    PaginatedDocumentDTO findDocuments(@RequestParam(required = false) UUID personId,
        @RequestParam(required = false) UUID type, @RequestParam(required = false) LocalDate fromDate,
        @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size);

    @PostExchange
    DocumentDTO uploadDocument(@RequestPart("document") MultipartFile document, @RequestParam("personId") UUID personId,
        @RequestParam("type") UUID type, @RequestParam(value = "date", required = false) LocalDate date,
        @RequestParam(value = "description", required = false) String description);

    @GetExchange("/{id}")
    ResponseEntity<byte[]> findDocumentById(@PathVariable UUID id);

    @PutExchange("/{id}")
    DocumentDTO updateDocument(@PathVariable UUID id, @RequestPart("document") MultipartFile document,
        @RequestParam("personId") UUID personId, @RequestParam("type") UUID type,
        @RequestParam(value = "date", required = false) LocalDate date,
        @RequestParam(value = "description", required = false) String description);

    @DeleteExchange("/{id}")
    void deleteDocument(@PathVariable UUID id);
}
