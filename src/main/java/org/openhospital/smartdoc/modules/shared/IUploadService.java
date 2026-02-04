package org.openhospital.smartdoc.modules.shared;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Port interface for file upload and storage operations.
 * Defines the contract for file management in the document system.
 */
public interface IUploadService {

    /**
     * Stores a file in the file system under the specified person directory.
     *
     * @param file the multipart file to store
     * @param personId the person identifier for organizing files
     * @param subDir the subdirectory within the person folder (e.g., "documents", "images")
     * @return the stored file path relative to the base directory
     * @throws IOException if file storage fails
     */
    String storeFile(MultipartFile file, UUID personId, String subDir) throws IOException;

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
     * Validates a file before upload.
     *
     * @param file the multipart file to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateFile(MultipartFile file);

    /**
     * Resolves the full file system path for a file.
     *
     * @param personId the person identifier
     * @param fileName the file name
     * @param subDir the subdirectory
     * @return the resolved Path
     */
    Path resolvePath(UUID personId, String fileName, String subDir);

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