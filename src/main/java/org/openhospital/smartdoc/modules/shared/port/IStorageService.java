package org.openhospital.smartdoc.modules.shared.port;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Port interface for core file storage operations.
 * Defines the contract for file system storage management.
 */
public interface IStorageService {

	/**
	 * Stores file content in the file system under the specified person directory.
	 *
	 * @param content  the file content as byte array
	 * @param filename the original filename
	 * @param subDir   the subdirectory within the person folder (e.g., "documents", "images")
	 * @return the stored file path relative to the base directory
	 * @throws IOException if file storage fails
	 */
	String storeFile(byte[] content, String filename, String subDir) throws IOException;

	/**
	 * Retrieves a file from the file system.
	 *
	 * @param filePath the relative file path
	 * @return Resource representing the file
	 */
	Resource retrieveFile(String filePath);

	/**
	 * Deletes a file from the file system.
	 *
	 * @param filePath the relative file path to delete
	 * @return true if file was deleted, false if it didn't exist
	 */
	boolean deleteFile(String filePath);

	/**
	 * Validates file content before storage.
	 *
	 * @param content  the file content
	 * @param filename the original filename
	 * @throws IllegalArgumentException if validation fails
	 */
	void validateFile(byte[] content, String filename);

	/**
	 * Gets the maximum allowed file size for uploads.
	 *
	 * @return maximum file size in bytes
	 */
	long getMaxFileSize();

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

	/**
	 * Extracts file extension from filename.
	 *
	 * @param filename the filename
	 * @return the file extension (without the dot), or empty string if no extension
	 */
	String getFileExtension(String filename);
}