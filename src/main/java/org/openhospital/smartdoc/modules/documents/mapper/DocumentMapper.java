package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.DocumentDTO;

@Mapper
public interface DocumentMapper {

    @Mapping(target = "personId", source = "person.id")
    @Mapping(target = "type", source = "documentType.code")
    DocumentDTO toDto(Document document);

    Document toModel(DocumentDTO documentDTO);
}