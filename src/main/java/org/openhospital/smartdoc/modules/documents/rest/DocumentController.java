package org.openhospital.smartdoc.modules.documents.rest;

import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.service.DocumentService;
import org.openhospital.smartdoc.openapi.DocumentMetadata;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.openhospital.smartdoc.types.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class DocumentController implements IDocumentService {
	private final IDocumentService service;

	public DocumentController(@Qualifier(DocumentService.NAME) IDocumentService service) {
		this.service = service;
	}

	@Override
	public Page<DocumentResponse> findDocuments(@RequestParam(required = false) UUID personId, @RequestParam(required = false) UUID type, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.findDocuments(personId, type, fromDate, toDate, page, size);
	}

	@Override
	public DocumentResponse findDocumentById(@PathVariable UUID id) {
		return service.findDocumentById(id);
	}

	@Override
	public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean attachment) {
		return service.downloadDocument(id, attachment);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocument(@PathVariable UUID id) {
		service.deleteDocument(id);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentResponse uploadDocument(@RequestPart MultipartFile document, @RequestPart DocumentMetadata metadata) {
		return service.uploadDocument(document, metadata);
	}

	@Override
	public DocumentResponse updateDocument(@PathVariable UUID id, @RequestPart MultipartFile document, @RequestPart DocumentMetadata metadata) {
		return service.updateDocument(id, document, metadata);
	}
}