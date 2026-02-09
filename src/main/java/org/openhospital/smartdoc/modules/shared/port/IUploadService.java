package org.openhospital.smartdoc.modules.shared.port;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Port interface for file upload and storage operations.
 * Defines the contract for file management in the document system.
 */
public interface IUploadService {

	/**
	 * Stores a file in the file system under the specified person directory.
	 *
	 * @param file   the multipart file to store
	 * @param subDir the subdirectory within the person folder (e.g., "documents", "images")
	 * @return the stored file path relative to the base directory
	 * @throws IOException if file storage fails
	 */
	String uploadFile(MultipartFile file, String subDir) throws IOException;

	/**
	 * Retrieves a file from the file system.
	 *
	 * @param filePath the relative file path
	 * @return Resource representing the file
	 */
	Resource retrieveFile(String filePath);

	/**
	 * Downloads a file with HTTP caching support.
	 *
	 * @param filePath   the relative file path
	 * @param attachment whether to force download (true) or allow inline viewing (false)
	 * @return ResponseEntity with file content and caching headers
	 */
	ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadFile(String filePath, boolean attachment);

	/**
	 * Deletes a file from the file system.
	 *
	 * @param filePath the relative file path to delete
	 * @return true if file was deleted, false if it didn't exist
	 */
	boolean deleteFile(String filePath);

	/**
	 * Validates a file before upload.
	 *
	 * @param file the multipart file to validate
	 * @throws IllegalArgumentException if validation fails
	 */
	void validateFile(MultipartFile file);

	/**
	 * Resolves the full file system path for a file.
	 *
	 * @param fileName the file name
	 * @param subDir   the subdirectory
	 * @return the resolved Path
	 */
	Path resolvePath(String fileName, String subDir);

	/**
	 * Checks if a file exists.
	 *
	 * @param filePath the relative file path
	 * @return true if file exists
	 */
	boolean fileExists(String filePath);

	/**
	 * Gets the file size.
	 *
	 * @param filePath the relative file path
	 * @return file size in bytes, or -1 if file doesn't exist
	 */
	long getFileSize(String filePath);
}