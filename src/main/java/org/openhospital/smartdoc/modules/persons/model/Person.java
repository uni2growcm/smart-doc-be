package org.openhospital.smartdoc.modules.persons.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.modules.documents.model.Document;
import org.openhospital.smartdoc.openapi.Status;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "persons")
public class Person extends BaseEntity {

	private String name;

	@Column(unique = true, nullable = false)
	private String pid;

	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status = Status.ACTIVE;

	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Document> documents = new ArrayList<>();
}
