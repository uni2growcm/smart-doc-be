package org.openhospital.smartdoc.modules.documents.mapper;

import org.openhospital.smartdoc.modules.documents.model.DocumentType;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentTypeMapper {

	public DocumentTypeResponse toDto(DocumentType type) {
		return new DocumentTypeResponse()
			.id(type.getId())
			.createdDate(type.getCreatedDate())
			.lastModifiedDate(type.getLastModifiedDate())
			.version(type.getVersion())
			.code(type.getCode())
			.name(type.getName())
			.description(type.getDescription())
			.status(type.getStatus());
	}

	public DocumentType toModel(CreateDocumentTypeRequest req) {
		return DocumentType.builder()
		                   .code(req.getCode())
		                   .name(req.getName())
		                   .description(req.getDescription())
		                   .build();
	}

	public void updateModel(UpdateDocumentTypeRequest req, DocumentType entity) {
		entity
			.setCode(req.getCode())
			.setName(req.getName())
			.setDescription(req.getDescription());
		entity.setVersion(req.getVersion());
	}

	public void patchModel(PatchDocumentTypeRequest req, DocumentType entity) {
		if (req.getVersion() != null) {
			entity.setVersion(req.getVersion());
		}
		if (req.getCode() != null) {
			entity.setCode(req.getCode());
		}
		if (req.getName() != null) {
			entity.setName(req.getName());
		}
		if (req.getDescription() != null) {
			entity.setDescription(req.getDescription());
		}
	}

	public List<DocumentTypeResponse> toDtos(List<DocumentType> types) {
		return types.stream().map(this::toDto).toList();
	}
}