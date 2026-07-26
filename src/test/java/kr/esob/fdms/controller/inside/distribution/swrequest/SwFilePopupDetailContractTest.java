package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwFilePopupDetailContractTest {

	@Test
	void popupLoadsServerVerifiedDocumentMetadataAfterAclCheck() throws Exception {
		String controller = read(
			"src/main/java/kr/esob/fdms/controller/inside/distribution/swrequest/SwRequestController.java");
		int popupStart = controller.indexOf("public String swFilePopup");
		int popupEnd = controller.indexOf("private String requirePopupViewAccess", popupStart);
		String popup = controller.substring(popupStart, popupEnd);

		int aclCheck = popup.indexOf("requirePopupViewAccess");
		int detailLookup = popup.indexOf("service.selectSwDetailInfo(resolvedObjectId)");
		assertTrue(aclCheck >= 0);
		assertTrue(detailLookup > aclCheck);
		assertTrue(popup.contains("model.addAttribute(\"documentInfo\""));
	}

	@Test
	void detailQueryReturnsReadableMetadataWithoutPhysicalFileColumns() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String detail = section(
			mapper, "<select id=\"selectSwDetailInfo\"", "<select id=\"selectSubFileInfo\"");

		assertTrue(detail.contains("AS \"swNm\""));
		assertTrue(detail.contains("AS \"classificationPath\""));
		assertTrue(detail.contains("AS \"businessTypeNm\""));
		assertTrue(detail.contains("AS \"insertUserNm\""));
		assertTrue(detail.contains("AS \"processingStatusNm\""));
		assertTrue(detail.contains("AS \"protectYnNm\""));
		assertTrue(detail.contains("AS \"validTypeNm\""));
		assertTrue(detail.contains("TO_CHAR(sw.INSERT_DT, 'YYYY-MM-DD HH24:MI')"));
		assertFalse(detail.contains("FILE_PATH_NM"));
		assertFalse(detail.contains("CHECKSUM"));
	}

	@Test
	void popupGroupsColumnsAndKeepsInternalIdentifiersOutOfTheDetailPanel() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swFilePopup.jsp");
		String detailPanel = section(
			page, "<section class=\"sw-detail-panel\"", "<!-- <div class=\"section popupCard");

		assertTrue(page.contains("<h2>기술자료 상세</h2>"));
		assertTrue(detailPanel.contains("문서 정보"));
		assertTrue(detailPanel.contains("등록·변경 이력"));
		assertTrue(detailPanel.contains("보안·승인 정보"));
		assertTrue(detailPanel.contains("<dt>CCB제목</dt>"));
		assertTrue(detailPanel.contains("<dt>자료분류</dt>"));
		assertTrue(detailPanel.contains("<dt>등록자</dt>"));
		assertTrue(detailPanel.contains("<dt>문서등급</dt>"));
		assertTrue(detailPanel.contains("<dt>승인자</dt>"));
		assertTrue(page.contains("grid-template-columns: repeat(4, minmax(0, 1fr))"));
		assertTrue(page.contains("@media (max-width: 900px)"));
		assertTrue(page.contains("@media (max-width: 600px)"));
		assertFalse(detailPanel.contains("objectId"));
		assertFalse(detailPanel.contains("filePath"));
		assertFalse(detailPanel.contains("checkSum"));
	}

	@Test
	void hiddenApprovalUiDoesNotTriggerAnExtraRequestAndHandlersStayIdempotent() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swFilePopup.jsp");

		assertTrue(page.contains(
			"if ($(\"#gridSwApproverStatus, #gridSwReviewerStatus\").length)"));
		assertTrue(page.contains(".off(\"click.swFilePopup\""));
		assertTrue(page.contains(".on(\"click.swFilePopup\""));
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}

	private String section(String source, String startMarker, String endMarker) {
		int start = source.indexOf(startMarker);
		assertTrue(start >= 0, "시작 구간을 찾을 수 없습니다: " + startMarker);
		int end = source.indexOf(endMarker, start + startMarker.length());
		assertTrue(end >= 0, "종료 구간을 찾을 수 없습니다: " + endMarker);
		return source.substring(start, end);
	}
}
