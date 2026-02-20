package org.openhospital.smartdoc.modules.persons.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openhospital.smartdoc.openapi.Gender;

/**
 * JPA AttributeConverter to persist Gender enum as a String in the database
 */
@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

	@Override
	public String convertToDatabaseColumn(Gender status) {
		return status != null ? status.getValue() : null;
	}

	@Override
	public Gender convertToEntityAttribute(String dbData) {
		return dbData != null ? Gender.fromValue(dbData) : null;
	}
}