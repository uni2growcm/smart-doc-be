package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@HttpExchange("/documents")
public interface IDocumentService {

	@GetExchange
	Page<DocumentResponse> findDocuments(@RequestParam int personId, @RequestParam(required = false) String type, @RequestParam(required = false) Instant fromDate, @RequestParam(required = false) Instant toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@GetExchange(value = "/{id}")
	ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable String id, @RequestParam int personId, @RequestParam String type, @RequestParam(defaultValue = "false") boolean attachment);

	@PostExchange
	DocumentResponse uploadDocument(@RequestPart MultipartFile document, @RequestParam int personId, @RequestParam String type, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
