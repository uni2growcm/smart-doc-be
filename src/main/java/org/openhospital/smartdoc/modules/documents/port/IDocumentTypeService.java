package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;

import java.util.List;
import java.util.UUID;

@HttpExchange("/document-types")
public interface IDocumentTypeService {

	@GetExchange
	List<DocumentTypeDTO> findDocumentTypes(@RequestParam(defaultValue = "false") boolean includeInactive);

	@PostExchange
	DocumentTypeDTO createDocumentType(@RequestBody CreateDocumentTypeRequestDTO payload);

	@GetExchange("/{id}")
	DocumentTypeDTO findDocumentTypeById(@PathVariable UUID id);

	@PutExchange("/{id}")
	DocumentTypeDTO updateDocumentType(@PathVariable UUID id, @RequestBody UpdateDocumentTypeRequestDTO payload);

	@PatchExchange("/{id}")
	DocumentTypeDTO patchDocumentType(@PathVariable UUID id, @RequestBody PatchDocumentTypeRequestDTO payload);

	@DeleteExchange("/{id}")
	void deleteDocumentType(@PathVariable UUID id);

	@PutExchange("/{id}/activate")
	DocumentTypeDTO activateDocumentType(@PathVariable UUID id);

	@PutExchange("/{id}/deactivate")
	DocumentTypeDTO deactivateDocumentType(@PathVariable UUID id);

	@PostExchange("/{id}/restore")
	DocumentTypeDTO restoreDocumentType(@PathVariable UUID id);
}
