package kr.esob.tdms.commonlogic.pdfconversion;

public final class PdfConversionClientResult {
    private final boolean reused;

    PdfConversionClientResult(boolean reused) {
        this.reused = reused;
    }

    public boolean isReused() {
        return reused;
    }
}
