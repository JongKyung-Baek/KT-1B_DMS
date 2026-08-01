package kr.esob.tdms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StoragePathUtilsTest {
    @Test
    void legacySeparatorsNormalizeForWindowsAndAix() {
        assertEquals("data/docs/file.pdf",
                StoragePathUtils.normalizeSeparators("data\\docs/file.pdf", '/'));
        assertEquals("data\\docs\\file.pdf",
                StoragePathUtils.normalizeSeparators("data\\docs/file.pdf", '\\'));
    }

    @Test
    void configuredRootResolutionUsesTheCurrentPlatformAndRejectsTraversal() {
        Path root = StoragePathUtils.toPath("target/storage-root").toAbsolutePath().normalize();
        Path resolved = StoragePathUtils.resolve(root.toString(), "\\nested/file.pdf");

        assertTrue(resolved.startsWith(root));
        assertEquals("file.pdf", resolved.getFileName().toString());
        assertEquals(File.separator + "nested" + File.separator + "file.pdf",
                resolved.toString().substring(root.toString().length()));
        assertThrows(IllegalArgumentException.class,
                () -> StoragePathUtils.resolve(root.toString(), "../outside.pdf"));
    }

    @Test
    void fileNameRemovesClientPathsOnWindowsAndAix() {
        assertEquals("manual.pdf", StoragePathUtils.fileName("C:\\upload\\manual.pdf"));
        assertEquals("manual.pdf", StoragePathUtils.fileName("/tmp/upload/manual.pdf"));
        assertThrows(IllegalArgumentException.class, () -> StoragePathUtils.fileName("../"));
        assertThrows(IllegalArgumentException.class, () -> StoragePathUtils.fileName("bad\rname.pdf"));
    }
}
