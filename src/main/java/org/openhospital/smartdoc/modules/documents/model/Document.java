package org.openhospital.smartdoc.modules.documents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.DocumentStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", referencedColumnName = "code")
    private DocumentType documentType;

    private LocalDate date;

    private String description;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "upload_date")
    private OffsetDateTime uploadDate;
}
