package kr.esob.tdms.commonlogic.pdfconversion;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class PdfConversionFiles {
    private PdfConversionFiles() {
    }

    static void requirePdf(Path pdf) {
        try {
            if (pdf == null || !Files.isRegularFile(pdf) || Files.size(pdf) < 8L) {
                throw new IllegalArgumentException("Converted PDF is unavailable.");
            }
            byte[] signature = new byte[5];
            try (InputStream input = Files.newInputStream(pdf)) {
                if (input.read(signature) != signature.length
                        || !"%PDF-".equals(new String(signature, StandardCharsets.US_ASCII))) {
                    throw new IllegalArgumentException("Converter response is not a PDF.");
                }
            }
            int tailSize = (int) Math.min(2048L, Files.size(pdf));
            byte[] tail = new byte[tailSize];
            try (RandomAccessFile randomAccess = new RandomAccessFile(pdf.toFile(), "r")) {
                randomAccess.seek(Files.size(pdf) - tailSize);
                randomAccess.readFully(tail);
            }
            if (!new String(tail, StandardCharsets.ISO_8859_1).contains("%%EOF")) {
                throw new IllegalArgumentException("Converted PDF is incomplete.");
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to validate the converted PDF.", exception);
        }
    }
}
