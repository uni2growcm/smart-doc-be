package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.DocumentMetadata;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.*;

import java.time.Instant;
import java.util.UUID;

@HttpExchange("/documents")
public interface IDocumentService {

	@GetExchange
	Page<DocumentResponse> findDocuments(@RequestParam(required = false) int personId, @RequestParam(required = false) String type, @RequestParam(required = false) Instant fromDate, @RequestParam(required = false) Instant toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size);

	@GetExchange("/{id}")
	DocumentResponse findDocumentById(@PathVariable String id);

	ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable String id, @RequestParam(defaultValue = "false") boolean attachment);

	DocumentResponse uploadDocument(@RequestPart MultipartFile document, @RequestPart DocumentMetadata metadata);

	void deleteDocument(@PathVariable String id);
}
