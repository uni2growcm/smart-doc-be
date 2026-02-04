package org.openhospital.smartdoc.modules.documents.port;

import java.util.List;
import java.util.UUID;
import org.openhospital.smartdoc.openapi.CreateDocumentTypeRequestDTO;
import org.openhospital.smartdoc.openapi.DocumentTypeDTO;
import org.openhospital.smartdoc.openapi.PatchDocumentTypeRequestDTO;
import org.openhospital.smartdoc.openapi.UpdateDocumentTypeRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

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
