package org.openhospital.smartdoc.modules.documents.rest;

import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.service.DocumentService;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.openhospital.smartdoc.types.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;

import java.time.Instant;
import java.time.LocalDate;

@RestController
public class DocumentController implements IDocumentService {
	private final IDocumentService service;

	public DocumentController(@Qualifier(DocumentService.NAME) IDocumentService service) {
		this.service = service;
	}

	public Page<DocumentResponse> findDocuments(@RequestParam int personId, @RequestParam(required = false) String type, @RequestParam(required = false) Instant fromDate, @RequestParam(required = false) Instant toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findDocuments(personId, type, fromDate, toDate, page, size);
	}

	@Override
	@GetExchange("/{*id}")
	public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable String id, @RequestParam(defaultValue = "false") boolean attachment) {
		return service.downloadDocument(id, attachment);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentResponse uploadDocument(@RequestPart MultipartFile document, @RequestParam int clientId, @RequestParam String type, @RequestParam(required = false) LocalDate date) {
		return service.uploadDocument(document, clientId, type, date);
	}
}