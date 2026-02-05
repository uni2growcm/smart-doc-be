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
	public List<DocumentTypeDTO> findDocumentTypes(@RequestParam(defaultValue = "false") boolean includeInactive) {
		return service.findDocumentTypes(includeInactive);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentTypeDTO createDocumentType(@RequestBody CreateDocumentTypeRequestDTO payload) {
		return service.createDocumentType(payload);
	}

	@Override
	public DocumentTypeDTO findDocumentTypeById(@PathVariable UUID id) {
		return service.findDocumentTypeById(id);
	}

	@Override
	public DocumentTypeDTO updateDocumentType(@PathVariable UUID id, @RequestBody UpdateDocumentTypeRequestDTO payload) {
		return service.updateDocumentType(id, payload);
	}

	@Override
	public DocumentTypeDTO patchDocumentType(@PathVariable UUID id, @RequestBody PatchDocumentTypeRequestDTO payload) {
		return service.patchDocumentType(id, payload);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocumentType(@PathVariable UUID id) {
		service.deleteDocumentType(id);
	}

	@Override
	public DocumentTypeDTO activateDocumentType(@PathVariable UUID id) {
		return service.activateDocumentType(id);
	}

	@Override
	public DocumentTypeDTO deactivateDocumentType(@PathVariable UUID id) {
		return service.deactivateDocumentType(id);
	}

	@Override
	public DocumentTypeDTO restoreDocumentType(@PathVariable UUID id) {
		return service.restoreDocumentType(id);
	}
}