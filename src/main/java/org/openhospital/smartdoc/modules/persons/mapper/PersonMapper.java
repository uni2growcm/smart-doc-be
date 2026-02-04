package org.openhospital.smartdoc.modules.persons.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.openhospital.smartdoc.config.MapperConfig;
import org.openhospital.smartdoc.modules.persons.model.Person;
import org.openhospital.smartdoc.openapi.CreatePersonRequestDTO;
import org.openhospital.smartdoc.openapi.PatchPersonRequestDTO;
import org.openhospital.smartdoc.openapi.PersonDTO;
import org.openhospital.smartdoc.openapi.UpdatePersonRequestDTO;

@Mapper(config = MapperConfig.class)
public interface PersonMapper {

    PersonDTO toDto(Person person);

    Person toModel(PersonDTO personDTO);

    Person toModel(CreatePersonRequestDTO req);

    void updateModel(UpdatePersonRequestDTO req, @MappingTarget Person entity);

    void patchModel(PatchPersonRequestDTO req, @MappingTarget Person entity);
}