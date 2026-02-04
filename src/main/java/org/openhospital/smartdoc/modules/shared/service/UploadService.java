package org.openhospital.smartdoc.modules.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.shared.port.IUploadService;
import org.openhospital.smartdoc.modules.shared.properties.UploadProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Service implementation for file upload and storage operations.
 * Handles file system operations with security and validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements IUploadService {

	// Allowed file extensions for security
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "txt", "rtf", "odt", "jpg", "jpeg", "png", "gif", "bmp", "tiff", "mp4", "avi", "mov", "wmv", "flv", "webm");
	// Maximum file size (200MB as configured)
	private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB
	private final UploadProperties properties;

	@Override
	public String storeFile(MultipartFile file, UUID personId, String subDir) throws IOException {
		validateFile(file);

		// Generate unique filename to prevent conflicts
		String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		String fileExtension = getFileExtension(originalFilename);
		String uniqueFilename = UUID.randomUUID() + "." + fileExtension;

		// Resolve the target path
		Path targetPath = resolvePath(personId, uniqueFilename, subDir);

		// Ensure parent directories exist
		Files.createDirectories(targetPath.getParent());

		// Copy file to target location
		try (var inputStream = file.getInputStream()) {
			Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}

		// Return relative path for storage in database
		Path basePath = Paths.get(properties.paths().baseDir());
		String relativePath = basePath.relativize(targetPath).toString();

		log.info("File stored successfully: {} -> {}", originalFilename, relativePath);
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
	public void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw CustomException.badRequest("uploads.errors.file-empty");
		}

		String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
			throw CustomException.badRequest("uploads.errors.file-invalid-name");
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw CustomException.badRequest("uploads.errors.file-too-large");
		}

		String extension = getFileExtension(filename).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw CustomException.badRequest("uploads.errors.file-type-not-allowed", new Object[]{extension});
		}
	}

	@Override
	public Path resolvePath(UUID personId, String fileName, String subDir) {
		return Paths.get(properties.paths().baseDir(), personId.toString(), subDir, fileName);
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

	/**
	 * Extracts file extension from filename.
	 */
	private String getFileExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
			return "";
		}
		return filename.substring(lastDotIndex + 1);
	}
}