package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.*;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.*;

@Mapper(config = MapperConfig.class)
public interface DocumentTypeMapper {

	DocumentTypeResponse toDto(DocumentType type);

	DocumentType toModel(DocumentTypeResponse typeDTO);

	DocumentType toModel(CreateDocumentTypeRequest req);

	void updateModel(UpdateDocumentTypeRequest req, @MappingTarget DocumentType entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void patchModel(PatchDocumentTypeRequest req, @MappingTarget DocumentType entity);
}
