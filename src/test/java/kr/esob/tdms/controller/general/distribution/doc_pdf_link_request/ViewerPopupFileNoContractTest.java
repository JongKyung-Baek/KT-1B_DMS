package kr.esob.tdms.controller.general.distribution.doc_pdf_link_request;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ViewerPopupFileNoContractTest {
    @Test
    void mainAndSubLinksAlwaysSendTheirActualRowFileNumber() throws Exception {
        String[] popupPaths = {
                "src/main/webapp/WEB-INF/views/general/distribution/drawingFilePopup.jsp",
                "src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp",
                "src/main/webapp/WEB-INF/views/general/distribution/production/productionFilePopup.jsp",
                "src/main/webapp/WEB-INF/views/general/distribution/dxf/dxfFilePopup.jsp"
        };

        for (String popupPath : popupPaths) {
            String jsp = Files.readString(Path.of(popupPath), StandardCharsets.UTF_8);
            assertThat(jsp)
                    .as(popupPath)
                    .contains("var fileNo = rowdata.fileNo || \"\";")
                    .contains("if (!objectId)")
                    .contains("objectId: \"\"")
                    .doesNotContain("useSubFileNo ? (rowdata.fileNo || \"\") : \"\"");
        }
    }
}
