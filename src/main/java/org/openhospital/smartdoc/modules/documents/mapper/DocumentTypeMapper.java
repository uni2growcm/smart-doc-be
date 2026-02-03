package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.DocumentTypeDTO;

@Mapper
public interface DocumentTypeMapper {

    DocumentTypeDTO toDto(DocumentType documentType);

    DocumentType toModel(DocumentTypeDTO documentTypeDTO);
}