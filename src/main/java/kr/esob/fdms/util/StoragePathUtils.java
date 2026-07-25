package kr.esob.fdms.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Converts legacy database paths to the separator of the current operating
 * system and joins configured storage roots without Windows-only literals.
 */
public final class StoragePathUtils {
    private StoragePathUtils() {
    }

    public static Path toPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage path is required.");
        }
        return Paths.get(normalizeSeparators(rawPath.trim(), File.separatorChar)).normalize();
    }

    public static Path resolve(String configuredRoot, String relativePath) {
        Path root = toPath(configuredRoot).toAbsolutePath().normalize();
        String child = normalizeSeparators(relativePath == null ? "" : relativePath.trim(),
                File.separatorChar);
        while (child.startsWith(File.separator)) {
            child = child.substring(1);
        }
        Path resolved = root.resolve(child).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Storage path escapes its configured root.");
        }
        return resolved;
    }

    public static String fileName(String rawName) {
        if (rawName == null) {
            throw new IllegalArgumentException("File name is required.");
        }
        String normalized = rawName.trim().replace('\\', '/');
        int separatorIndex = normalized.lastIndexOf('/');
        String name = separatorIndex >= 0 ? normalized.substring(separatorIndex + 1) : normalized;
        if (name.isEmpty() || ".".equals(name) || "..".equals(name)
                || name.indexOf('\0') >= 0 || name.indexOf('\r') >= 0 || name.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        return name;
    }

    static String normalizeSeparators(String value, char separator) {
        if (value == null) {
            return "";
        }
        return value.replace('\\', separator).replace('/', separator);
    }
}
