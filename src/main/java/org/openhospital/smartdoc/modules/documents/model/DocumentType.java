package org.openhospital.smartdoc.modules.documents.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.openapi.Status;

@Data
@Entity
@Table(name = "document_types")
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DocumentType extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	private String description;

	@Builder.Default
	private Status status = Status.ACTIVE;
}
