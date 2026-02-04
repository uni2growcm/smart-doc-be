package org.openhospital.smartdoc.modules.documents.port;

import org.openhospital.smartdoc.openapi.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

import java.util.List;
import java.util.UUID;

@HttpExchange("/document-types")
public interface IDocumentTypeService {

	@GetExchange
	List<DocumentTypeDTO> getDocumentTypes();

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
}
