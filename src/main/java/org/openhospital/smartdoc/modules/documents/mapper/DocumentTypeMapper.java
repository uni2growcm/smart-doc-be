package org.openhospital.smartdoc.modules.documents.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.*;

import java.time.*;

@Mapper(config = MapperConfig.class)
public interface DocumentTypeMapper {

	DocumentTypeResponse toDto(DocumentType type);

	DocumentType toModel(DocumentTypeResponse typeDTO);

	DocumentType toModel(CreateDocumentTypeRequest req);

	void updateModel(UpdateDocumentTypeRequest req, @MappingTarget DocumentType entity);

	void patchModel(PatchDocumentTypeRequest req, @MappingTarget DocumentType entity);

	// Date conversion methods
	default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
		return offsetDateTime != null ? offsetDateTime.toInstant() : null;
	}

	default OffsetDateTime instantToOffsetDateTime(Instant instant) {
		return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
	}
}
