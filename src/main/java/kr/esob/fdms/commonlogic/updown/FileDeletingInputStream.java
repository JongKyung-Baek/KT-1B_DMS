package kr.esob.fdms.commonlogic.updown;
import java.io.*;


// 2023. 07. 13 기범추가
// 파일 다운로드 Class. FileDownloadController 랑 세트

public class FileDeletingInputStream extends FileInputStream {

    private final File file;

    public FileDeletingInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (!file.delete()) {
                throw new IOException("Could not delete file " + file);
            }
        }
    }


}
