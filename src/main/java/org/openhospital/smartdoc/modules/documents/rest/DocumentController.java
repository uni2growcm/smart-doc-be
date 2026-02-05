package org.openhospital.smartdoc.modules.documents.rest;

import lombok.RequiredArgsConstructor;
import org.openhospital.smartdoc.modules.documents.port.IDocumentService;
import org.openhospital.smartdoc.modules.documents.service.DocumentService;
import org.openhospital.smartdoc.openapi.DocumentDTO;
import org.openhospital.smartdoc.types.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class DocumentController implements IDocumentService {
	private final DocumentService service;

	@Override
	public Page<DocumentDTO> findDocuments(UUID personId, UUID type, LocalDate fromDate, LocalDate toDate, int page, int size) {
		return service.findDocuments(personId, type, fromDate, toDate, page, size);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentDTO uploadDocument(MultipartFile document, UUID personId, UUID type, LocalDate date, String description) {
		return service.uploadDocument(document, personId, type, date, description);
	}

	@Override
	public DocumentDTO findDocumentById(UUID id) {
		return service.findDocumentById(id);
	}

	@Override
	public ResponseEntity<ByteArrayResource> downloadDocument(UUID id, boolean attachment) {
		return service.downloadDocument(id, attachment);
	}

	@Override
	public DocumentDTO updateDocument(UUID id, MultipartFile document, UUID personId, UUID type, LocalDate date, String description) {
		return service.updateDocument(id, document, personId, type, date, description);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocument(UUID id) {
		service.deleteDocument(id);
	}
}