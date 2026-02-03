package org.openhospital.smartdoc.modules.persons.mapper;

import org.mapstruct.Mapper;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.PersonDTO;

@Mapper
public interface PersonMapper {

    PersonDTO toDto(Person person);

    Person toModel(PersonDTO personDTO);
}