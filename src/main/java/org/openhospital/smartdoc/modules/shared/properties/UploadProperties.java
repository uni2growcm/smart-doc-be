package org.openhospital.smartdoc.modules.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for file upload functionality.
 * Maps to application properties with prefix "uploads".
 */
@ConfigurationProperties(prefix = "uploads")
public record UploadProperties(
	Paths paths,
	Storage storage
) {
	/**
	 * File system paths configuration.
	 */
	public record Paths(
		String baseDir,
		String videos,
		String images,
		String documents
	) {
	}

	/**
	 * Storage configuration.
	 */
	public record Storage(
		int maxItems
	) {
	}
}