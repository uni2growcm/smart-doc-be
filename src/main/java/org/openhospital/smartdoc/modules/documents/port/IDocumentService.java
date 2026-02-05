package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.DocumentDTO;
import org.openhospital.smartdoc.openapi.DocumentMetadataDTO;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
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

	@GetExchange("/{id}")
	DocumentDTO findDocumentById(@PathVariable UUID id);

	@GetExchange("/{id}/download")
	ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean attachment);

	@PostExchange
	DocumentDTO uploadDocument(@RequestPart MultipartFile document, @RequestPart DocumentMetadataDTO metadata);

	@PutExchange("/{id}")
	DocumentDTO updateDocument(@PathVariable UUID id, @RequestPart MultipartFile document, @RequestPart DocumentMetadataDTO metadata);

	@DeleteExchange("/{id}")
	void deleteDocument(@PathVariable UUID id);
}
