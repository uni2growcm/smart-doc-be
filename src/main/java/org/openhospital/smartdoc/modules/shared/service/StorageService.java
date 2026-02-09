package org.openhospital.smartdoc.modules.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.helpers.DateUtils;
import org.openhospital.smartdoc.modules.shared.port.IStorageService;
import org.openhospital.smartdoc.modules.shared.properties.StorageProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation for core file storage operations.
 * Handles file system storage, retrieval, and validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService implements IStorageService {
	// Maximum file size (200MB as configured)

	private final StorageProperties properties;

	@Override
	public String storeFile(byte[] content, String filename, String subDir, LocalDate date) throws IOException {
		validateFile(content, filename);

		// Generate unique filename to prevent conflicts
		String datePrefix = DateUtils.format(date == null ? LocalDate.now() : date, DateTimeFormatter.BASIC_ISO_DATE);

		String cleanFilename = org.springframework.util.StringUtils.cleanPath(filename);
		String uniqueFilename = "%s_%s"
			.formatted(datePrefix, cleanFilename);

		// Resolve the target path
		Path targetPath = resolvePath(uniqueFilename, subDir);

		// Ensure parent directories exist
		Files.createDirectories(targetPath.getParent());

		// Write file to target location
		Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

		// Return relative path for storage in database
		Path basePath = Paths.get(properties.paths().baseDir());
		String relativePath = basePath.relativize(targetPath).toString();

		log.info("File stored successfully: {} -> {}", cleanFilename, relativePath);
		return relativePath;
	}

	@Override
	public Resource retrieveFile(String filePath) {
		Path fullPath = Paths.get(properties.paths().baseDir(), filePath);

		// Security check: ensure path is within allowed directory
		Path basePath = Paths.get(properties.paths().baseDir());
		if (!fullPath.startsWith(basePath)) {
			throw CustomException.forbidden("uploads.errors.path-traversal-detected");
		}

		if (!Files.exists(fullPath)) {
			throw CustomException.notFound("uploads.errors.file-not-found");
		}

		return new FileSystemResource(fullPath);
	}

	@Override
	public boolean deleteFile(String filePath) {
		Path fullPath = Paths.get(properties.paths().baseDir(), filePath);

		if (!Files.exists(fullPath)) {
			log.warn("File not found for deletion: {}", filePath);
			throw CustomException.notFound("uploads.errors.file-not-found");
		}

		// Security check
		Path basePath = Paths.get(properties.paths().baseDir());
		if (!fullPath.startsWith(basePath)) {
			throw CustomException.forbidden("uploads.errors.path-traversal-detected");
		}

		try {
			boolean deleted = Files.deleteIfExists(fullPath);
			if (deleted) {
				log.info("File deleted successfully: {}", filePath);
			}
			return deleted;
		} catch (IOException e) {
			log.error("Failed to delete file: {}", filePath, e);
			throw CustomException.internal("uploads.errors.file-deletion-failed");
		}
	}

	@Override
	public void validateFile(byte[] content, String filename) {
		if (content == null || content.length == 0) {
			throw CustomException.badRequest("uploads.errors.file-empty");
		}

		String cleanFilename = org.springframework.util.StringUtils.cleanPath(filename);
		if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
			throw CustomException.badRequest("uploads.errors.file-invalid-name");
		}

		if (content.length > getMaxFileSize()) {
			throw CustomException.badRequest("uploads.errors.file-too-large");
		}

		String extension = getFileExtension(cleanFilename).toLowerCase();
	}

	@Override
	public long getMaxFileSize() {
		return properties.storage().maxFileSize() * 1024L * 1024L; // Convert MB to bytes
	}

	@Override
	public Path resolvePath(String fileName, String subDir) {
		return Paths.get(properties.paths().baseDir(), subDir, fileName);
	}

	@Override
	public boolean fileExists(String filePath) {
		Path fullPath = Paths.get(properties.paths().baseDir(), filePath);
		Path basePath = Paths.get(properties.paths().baseDir());

		// Security check
		if (!fullPath.startsWith(basePath)) {
			return false;
		}

		return Files.exists(fullPath);
	}

	@Override
	public long getFileSize(String filePath) {
		Path fullPath = Paths.get(properties.paths().baseDir(), filePath);
		Path basePath = Paths.get(properties.paths().baseDir());

		// Security check
		if (!fullPath.startsWith(basePath)) {
			return -1;
		}

		try {
			return Files.size(fullPath);
		} catch (IOException e) {
			return -1;
		}
	}

	@Override
	public String getFileExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
			return "";
		}
		return filename.substring(lastDotIndex + 1);
	}
}