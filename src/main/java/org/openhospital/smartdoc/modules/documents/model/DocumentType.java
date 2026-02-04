package org.openhospital.smartdoc.modules.documents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.openapi.Status;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "document_types")
public class DocumentType extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.ACTIVE;
}
