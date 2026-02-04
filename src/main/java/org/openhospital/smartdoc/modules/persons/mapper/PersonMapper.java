package org.openhospital.smartdoc.modules.persons.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.*;

import java.time.*;
import java.util.List;

@Mapper(config = MapperConfig.class)
public interface PersonMapper {

	PersonDTO toDto(Person person);

	List<PersonDTO> toDtos(List<Person> persons);

	Person toModel(PersonDTO personDTO);

	Person toModel(CreatePersonRequestDTO req);

	void updateModel(UpdatePersonRequestDTO req, @MappingTarget Person entity);

	void patchModel(PatchPersonRequestDTO req, @MappingTarget Person entity);

	// Date conversion methods
	default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
		return offsetDateTime != null ? offsetDateTime.toInstant() : null;
	}

	default OffsetDateTime instantToOffsetDateTime(Instant instant) {
		return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
	}
}