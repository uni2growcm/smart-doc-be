package org.openhospital.smartdoc.helpers;

public abstract class FilesUtils {
    public static String normalizeToSlash(String path) {
        return  path != null ? path.replace("\\", "/") : null;
    }
}