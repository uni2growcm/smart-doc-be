package org.openhospital.smartdoc.config;

import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@org.mapstruct.MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapperConfig {

}
