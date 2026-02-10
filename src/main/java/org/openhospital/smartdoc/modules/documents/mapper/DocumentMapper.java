package org.openhospital.smartdoc.modules.documents.mapper;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.openhospital.smartdoc.openapi.DocumentResponse;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting file paths to DocumentResponse objects.
 */
@Component
@RequiredArgsConstructor
public class DocumentMapper {

	private static @NonNull LocalDate getLocalDate(String[] parts) {
		String filename = parts[4];
		int underscoreIndex = filename.indexOf('_');
		if (underscoreIndex == -1) {
			throw new IllegalArgumentException("Invalid filename format: " + filename);
		}

		// Parse date from filename prefix
		String dateStr = filename.substring(0, underscoreIndex);
		if (dateStr.length() != 8) {
			throw new IllegalArgumentException("Invalid date format in filename: " + dateStr);
		}
		LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
		return date;
	}

	/**
	 * Converts a file path to a DocumentResponse by parsing filesystem metadata.
	 *
	 * @param filePath the absolute path to the document file
	 * @param baseDir  the base directory for relative path calculation
	 * @return DocumentResponse with extracted metadata
	 */
	public DocumentResponse toDto(Path filePath, String baseDir) {
		String relativePath = Paths.get(baseDir).relativize(filePath).toString();
		String[] parts = relativePath.split("/");

		if (parts.length != 5) {
			throw new IllegalArgumentException("Invalid document path structure: " + relativePath);
		}

		// Parse personId from xx/xx/xx
		String clientPath = parts[0] + parts[1] + parts[2];
		int personId = Integer.parseInt(clientPath);

		String type = parts[3];
		String id = parts[4];

		// Parse filename: YYYYMMDD_filename.ext
		LocalDate date = getLocalDate(parts);

		return new DocumentResponse()
			.id(id)
			.personId(personId)
			.type(type)
			.date(date);
	}
}