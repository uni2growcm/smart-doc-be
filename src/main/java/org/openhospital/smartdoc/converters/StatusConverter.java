package org.openhospital.smartdoc.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openhospital.smartdoc.openapi.Status;

/**
 * JPA AttributeConverter to persist Status enum as a String in the database
 */
@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

	@Override
	public String convertToDatabaseColumn(Status status) {
		return status != null ? status.getValue() : null;
	}

	@Override
	public Status convertToEntityAttribute(String dbData) {
		return dbData != null ? Status.fromValue(dbData) : null;
	}
}