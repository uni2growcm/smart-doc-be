package org.openhospital.smartdoc.modules.persons.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.openhospital.smartdoc.models.BaseEntity;
import org.openhospital.smartdoc.openapi.Gender;
import org.openhospital.smartdoc.openapi.Status;

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
	private int pid;

	private String email;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private Status status = Status.ACTIVE;
}
