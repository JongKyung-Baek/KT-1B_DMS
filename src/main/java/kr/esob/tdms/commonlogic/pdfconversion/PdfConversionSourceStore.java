package kr.esob.tdms.commonlogic.pdfconversion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;

import kr.esob.tdms.commonlogic.fileapi.FileApiClient;
import kr.esob.tdms.controller.general.distribution.swrequest.TechnicalFileTypePolicy;

@Component
public class PdfConversionSourceStore {
    private final PdfConversionProperties properties;
    private final FileApiClient fileApiClient = new FileApiClient();

    public PdfConversionSourceStore(PdfConversionProperties properties) {
        this.properties = properties;
    }

    public Path createWorkFile(String suffix) {
        try {
            Path work = properties.workPath();
            Files.createDirectories(work);
            String safeSuffix = suffix == null || !suffix.matches("\\.[A-Za-z0-9]{1,16}")
                    ? ".bin" : suffix.toLowerCase();
            return Files.createTempFile(work, "pdf-job-" + UUID.randomUUID() + "-", safeSuffix);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create the PDF conversion work file.", exception);
        }
    }

    public Path materialize(String storedPath, String suffix) {
        Path target = createWorkFile(suffix);
        boolean completed = false;
        try {
            String normalized = storedPath == null ? "" : storedPath.trim().replace('\\', '/');
            String[] repositoryPath = TechnicalFileTypePolicy.splitRepositoryPath(normalized);
            if (repositoryPath != null) {
                fileApiClient.downloadTo(repositoryPath[1], repositoryPath[0], target);
            } else {
                if (normalized.isEmpty()) {
                    throw new IllegalArgumentException("Stored file path is empty.");
                }
                Path source = Paths.get(storedPath).toAbsolutePath().normalize();
                if (!Files.isRegularFile(source)) {
                    throw new IllegalArgumentException("Stored source file is unavailable.");
                }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            completed = true;
            return target;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to materialize the conversion source.", exception);
        } finally {
            if (!completed) {
                deleteQuietly(target);
            }
        }
    }

    public String saveConvertedPdf(Path pdf, String sourceSha256) {
        PdfConversionFiles.requirePdf(pdf);
        String fileName = sourceSha256.toLowerCase() + ".pdf";
        String folder = properties.getOutputFolder() == null
                ? "" : properties.getOutputFolder().trim();
        if (!folder.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}") || folder.contains("..")) {
            throw new IllegalStateException("PDF conversion output folder is invalid.");
        }
        fileApiClient.upload(pdf.toFile(), fileName, folder);
        return folder + "/" + fileName;
    }

    public static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

    public static String extensionSuffix(String fileName) {
        String extension = TechnicalFileTypePolicy.extensionOf(fileName);
        return extension.isEmpty() ? ".bin" : "." + extension;
    }
}
