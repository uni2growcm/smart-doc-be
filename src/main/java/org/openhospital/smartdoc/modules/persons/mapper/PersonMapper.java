package org.openhospital.smartdoc.modules.persons.mapper;

import org.mapstruct.*;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.*;

@Mapper(config = MapperConfig.class)
public interface PersonMapper {

	PersonResponse toDto(Person person);

	Person toModel(PersonResponse personDTO);

	Person toModel(CreatePersonRequest req);

	void updateModel(UpdatePersonRequest req, @MappingTarget Person entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void patchModel(PatchPersonRequest req, @MappingTarget Person entity);
}