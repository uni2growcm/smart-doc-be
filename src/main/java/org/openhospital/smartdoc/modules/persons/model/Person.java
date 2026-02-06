package org.openhospital.smartdoc.modules.persons.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.Gender;
import org.openhospital.smartdoc.openapi.Status;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "persons")
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Person extends BaseEntity {

	private String name;

	@Column(unique = true, nullable = false)
	private String pid;

	private String email;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private Status status = Status.ACTIVE;

	@Builder.Default
	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Document> documents = new ArrayList<>();
}
