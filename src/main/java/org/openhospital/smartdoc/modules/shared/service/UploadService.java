package org.openhospital.smartdoc.modules.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openhospital.smartdoc.exceptions.CustomException;
import org.openhospital.smartdoc.modules.shared.port.IStorageService;
import org.openhospital.smartdoc.modules.shared.port.IUploadService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Service implementation for file upload and storage operations.
 * Handles file system operations with security and validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements IUploadService {

	private final IStorageService storageService;

	@Override
	public String uploadFile(MultipartFile file, String subDir, LocalDate date) throws IOException {
		validateFile(file);

		// Extract file content as bytes
		byte[] content = file.getBytes();

		// Get original filename
		String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

		// Delegate to storage service for actual storage
		return storageService.storeFile(content, originalFilename, subDir, date);
	}

	@Override
	public Resource retrieveFile(String filePath) {
		return storageService.retrieveFile(filePath);
	}

	@Override
	public ResponseEntity<ByteArrayResource> downloadFile(String filePath, boolean attachment) {
		try {
			Resource resource = storageService.retrieveFile(filePath);
			FileSystemResource fileResource = (FileSystemResource) resource;
			Path filePathObj = Paths.get(fileResource.getPath());

			// Read file content
			byte[] content = Files.readAllBytes(filePathObj);
			ByteArrayResource byteArrayResource = new ByteArrayResource(content);

			// Get file metadata for caching
			long fileSize = content.length;
			long lastModifiedTimestamp = Files.getLastModifiedTime(filePathObj).toMillis();

			// Generate ETag: "fileSize-lastModifiedTimestamp"
			String eTag = "\"" + fileSize + "-" + lastModifiedTimestamp + "\"";

			// Extract filename from path for Content-Disposition
			String filename = filePathObj.getFileName().toString();

			// Build response headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(Files.probeContentType(filePathObj)));
			headers.setContentLength(fileSize);
			headers.setETag(eTag);
			headers.setLastModified(lastModifiedTimestamp);

			if (attachment) {
				headers.setContentDispositionFormData("attachment", filename);
			} else {
				headers.setContentDispositionFormData("inline", filename);
			}

			return ResponseEntity.ok().headers(headers).body(byteArrayResource);

		} catch (IOException e) {
			log.error("Failed to download file: {}", filePath, e);
			throw CustomException.internal("uploads.errors.file-download-failed");
		}
	}

	@Override
	public boolean deleteFile(String filePath) {
		return storageService.deleteFile(filePath);
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


		if (file.getSize() > storageService.getMaxFileSize()) {
			throw CustomException.badRequest("uploads.errors.file-too-large");
		}

		String extension = storageService.getFileExtension(filename).toLowerCase();
	}

	@Override
	public Path resolvePath(String fileName, String subDir) {
		return storageService.resolvePath(fileName, subDir);
	}

	@Override
	public boolean fileExists(String filePath) {
		return storageService.fileExists(filePath);
	}

	@Override
	public long getFileSize(String filePath) {
		return storageService.getFileSize(filePath);
	}
}