package kr.esob.tdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class CommonJsViewerPreflightContractTest {

	@Test
	void bothDestroyStatusPreflightsIncludeTheSelectedFileNumber() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/resources/static/js/common.js")), StandardCharsets.UTF_8);
		int openFileStart = source.indexOf("function openFile(");
		int openFileEnd = source.indexOf("function openDownHistoryPopup", openFileStart);
		assertTrue(openFileStart >= 0);
		assertTrue(openFileEnd > openFileStart);
		String openFile = source.substring(openFileStart, openFileEnd);
		String payload = "{ objectId: objectId, objectType: objectType, "
				+ "requestType: requestType, requestNo: requestNo, fileNo: fileNo }";

		assertTrue(openFile.contains("checkBoolean(" + payload
				+ ", \"/common/viewer/getDestroyStatus\")"));
		assertTrue(openFile.contains("checkBoolean(" + payload
				+ ", \"/common/viewer/getDestroyStatus_printHistory\")"));
	}
}
