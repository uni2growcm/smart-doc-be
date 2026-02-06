package org.openhospital.smartdoc.modules.persons.mapper;

import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonMapper {

	public PersonResponse toDto(Person person) {
		return new PersonResponse().id(person.getId()).createdDate(person.getCreatedDate()).lastModifiedDate(person.getLastModifiedDate()).version(person.getVersion()).name(person.getName()).pid(person.getPid()).email(person.getEmail()).phoneNumber(person.getPhoneNumber()).gender(person.getGender()).status(person.getStatus());
	}

	public Person toModel(CreatePersonRequest req) {
		return Person.builder().name(req.getName()).pid(req.getPid()).email(req.getEmail()).phoneNumber(req.getPhoneNumber()).gender(req.getGender()).build();
	}

	public void updateModel(UpdatePersonRequest req, Person entity) {
		entity
			.setName(req.getName())
			.setPid(req.getPid())
			.setEmail(req.getEmail())
			.setPhoneNumber(req.getPhoneNumber())
			.setGender(req.getGender());
		if (req.getVersion() != null) {
			entity.setVersion(req.getVersion());
		}
	}

	public void patchModel(PatchPersonRequest req, Person entity) {
		if (req.getVersion() != null) {
			entity.setVersion(req.getVersion());
		}
		if (req.getName() != null) {
			entity.setName(req.getName());
		}
		if (req.getPid() != null) {
			entity.setPid(req.getPid());
		}
		if (req.getEmail() != null) {
			entity.setEmail(req.getEmail());
		}
		if (req.getPhoneNumber() != null) {
			entity.setPhoneNumber(req.getPhoneNumber());
		}
		if (req.getGender() != null) {
			entity.setGender(req.getGender());
		}
	}

	public List<PersonResponse> toDtos(List<Person> persons) {
		return persons.stream().map(this::toDto).toList();
	}
}