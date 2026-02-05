package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.DocumentResponse;

@Mapper(config = MapperConfig.class)
public interface DocumentMapper {

	@Mapping(target = "personId", expression = "java(document.getPerson().getId().toString())")
	@Mapping(target = "type", expression = "java(document.getType().getId().toString())")
	@Mapping(target = "fileSize", expression = "java(document.getFileSize() != null ? document.getFileSize().intValue() : null)")
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "version", ignore = true)
	DocumentResponse toDto(Document document);
}