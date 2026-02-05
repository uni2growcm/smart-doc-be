package org.openhospital.smartdoc.modules.documents.rest;

import org.openhospital.smartdoc.modules.documents.port.IDocumentTypeService;
import org.openhospital.smartdoc.modules.documents.service.DocumentTypeService;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class DocumentTypeController implements IDocumentTypeService {
	private final IDocumentTypeService service;

	public DocumentTypeController(@Qualifier(DocumentTypeService.NAME) IDocumentTypeService service) {
		this.service = service;
	}

	@Override
	public List<DocumentTypeResponse> findDocumentTypes(@RequestParam(defaultValue = "false") boolean includeInactive) {
		return service.findDocumentTypes(includeInactive);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentTypeResponse createDocumentType(@RequestBody CreateDocumentTypeRequest payload) {
		return service.createDocumentType(payload);
	}

	@Override
	public DocumentTypeResponse findDocumentTypeById(@PathVariable UUID id) {
		return service.findDocumentTypeById(id);
	}

	@Override
	public DocumentTypeResponse updateDocumentType(@PathVariable UUID id, @RequestBody UpdateDocumentTypeRequest payload) {
		return service.updateDocumentType(id, payload);
	}

	@Override
	public DocumentTypeResponse patchDocumentType(@PathVariable UUID id, @RequestBody PatchDocumentTypeRequest payload) {
		return service.patchDocumentType(id, payload);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocumentType(@PathVariable UUID id) {
		service.deleteDocumentType(id);
	}

	@Override
	public DocumentTypeResponse activateDocumentType(@PathVariable UUID id) {
		return service.activateDocumentType(id);
	}

	@Override
	public DocumentTypeResponse deactivateDocumentType(@PathVariable UUID id) {
		return service.deactivateDocumentType(id);
	}

	@Override
	public DocumentTypeResponse restoreDocumentType(@PathVariable UUID id) {
		return service.restoreDocumentType(id);
	}
}