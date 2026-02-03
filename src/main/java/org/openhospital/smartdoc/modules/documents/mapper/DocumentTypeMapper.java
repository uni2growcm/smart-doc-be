package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.CreateDocumentTypeRequestDTO;
import org.openhospital.smartdoc.openapi.DocumentTypeDTO;
import org.openhospital.smartdoc.openapi.PatchDocumentTypeRequestDTO;
import org.openhospital.smartdoc.openapi.UpdateDocumentTypeRequestDTO;

@Mapper(config = MapperConfig.class)
public interface DocumentTypeMapper {

    DocumentTypeDTO toDto(DocumentType documentType);

    DocumentType toModel(DocumentTypeDTO documentTypeDTO);

    DocumentType toModel(CreateDocumentTypeRequestDTO req);

    void updateModel(UpdateDocumentTypeRequestDTO req, @MappingTarget DocumentType entity);

    void patchModel(PatchDocumentTypeRequestDTO req, @MappingTarget DocumentType entity);
}
