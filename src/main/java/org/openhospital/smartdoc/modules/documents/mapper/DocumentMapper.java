package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.DocumentDTO;

import java.util.List;

@Mapper(config = MapperConfig.class)
public interface DocumentMapper {

	@Mapping(target = "personId", source = "person.id")
	@Mapping(target = "type", source = "type.id")
	@Mapping(target = "fileSize", expression = "java(document.getFileSize() != null ? document.getFileSize().intValue() : null)")
	DocumentDTO toDto(Document document);

	List<DocumentDTO> toDtos(List<Document> documents);

	// Note: DocumentDTO is generated from OpenAPI and doesn't have setters for creation
	// We'll handle document creation manually in the service
}
