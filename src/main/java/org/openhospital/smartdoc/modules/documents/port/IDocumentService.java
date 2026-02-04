package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.DocumentDTO;
import org.openhospital.smartdoc.types.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@HttpExchange("/documents")
public interface IDocumentService {

	@GetExchange
	Page<DocumentDTO> findDocuments(@RequestParam(required = false) UUID personId, @RequestParam(required = false) UUID type, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@PostExchange
	DocumentDTO uploadDocument(@RequestPart("document") MultipartFile document, @RequestParam("personId") UUID personId, @RequestParam("type") UUID type, @RequestParam(value = "date", required = false) LocalDate date, @RequestParam(value = "description", required = false) String description);

	@GetExchange("/{id}")
	ResponseEntity<byte[]> findDocumentById(@PathVariable UUID id);

	@PutExchange("/{id}")
	DocumentDTO updateDocument(@PathVariable UUID id, @RequestPart("document") MultipartFile document, @RequestParam("personId") UUID personId, @RequestParam("type") UUID type, @RequestParam(value = "date", required = false) LocalDate date, @RequestParam(value = "description", required = false) String description);

	@DeleteExchange("/{id}")
	void deleteDocument(@PathVariable UUID id);
}
