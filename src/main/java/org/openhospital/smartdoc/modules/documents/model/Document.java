package org.openhospital.smartdoc.modules.documents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.DocumentStatus;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

	@Column(name = "file_name")
	private String fileName;

	private String path;

	@ManyToOne(fetch = FetchType.LAZY)
	private Person person;

	@ManyToOne(fetch = FetchType.LAZY)
	private DocumentType type;

	private Instant date;

	private String description;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "mime_type")
	private String mimeType;

	@Enumerated(EnumType.STRING)
	private DocumentStatus status;

	@Column(name = "upload_date")
	private Instant uploadDate;
}
