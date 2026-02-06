package org.openhospital.smartdoc.modules.documents.mapper;

import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentMapper {

    public DocumentResponse toDto(Document document) {
        return new DocumentResponse()
                .id(document.getId())
                .fileName(document.getFileName())
                .path(document.getPath())
                .personId(document.getPerson().getId().toString())
                .type(document.getType().getId().toString())
                .date(document.getDate())
                .description(document.getDescription())
                .fileSize(document.getFileSize() != null ? document.getFileSize().intValue() : null)
                .mimeType(document.getMimeType())
                .status(document.getStatus())
                .uploadDate(document.getUploadDate());
    }

    public List<DocumentResponse> toDtos(List<Document> documents) {
        return documents.stream().map(this::toDto).toList();
    }
}