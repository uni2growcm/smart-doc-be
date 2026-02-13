package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

import java.util.List;
import java.util.UUID;

@HttpExchange("/document-types")
public interface IDocumentTypeService {

	@GetExchange
	List<DocumentTypeResponse> findDocumentTypes(@RequestParam(defaultValue = "false") boolean includeInactive);

	@PostExchange
	DocumentTypeResponse createDocumentType(@RequestBody CreateDocumentTypeRequest payload);

	@GetExchange("/{id}")
	DocumentTypeResponse findDocumentTypeById(@PathVariable UUID id);

	@PutExchange("/{id}")
	DocumentTypeResponse updateDocumentType(@PathVariable UUID id, @RequestBody UpdateDocumentTypeRequest payload);

	@PatchExchange("/{id}")
	DocumentTypeResponse patchDocumentType(@PathVariable UUID id, @RequestBody PatchDocumentTypeRequest payload);

	@DeleteExchange("/{id}")
	void deleteDocumentType(@PathVariable UUID id);

	@PutExchange("/{id}/activate")
	DocumentTypeResponse activateDocumentType(@PathVariable UUID id);

	@PutExchange("/{id}/deactivate")
	DocumentTypeResponse deactivateDocumentType(@PathVariable UUID id);

	@PostExchange("/{id}/restore")
	DocumentTypeResponse restoreDocumentType(@PathVariable UUID id);
}
