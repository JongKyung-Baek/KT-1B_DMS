package kr.esob.tdms.controller.general.distribution.swrequest;

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
			"src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");
		int popupStart = controller.indexOf("public String swFilePopup");
		int popupEnd = controller.indexOf("private String requirePopupViewAccess", popupStart);
		String popup = controller.substring(popupStart, popupEnd);

		int aclCheck = popup.indexOf("requirePopupViewAccess");
		int detailLookup = popup.indexOf("service.selectSwDetailInfo(resolvedObjectId)");
		assertTrue(aclCheck >= 0);
		assertTrue(detailLookup > aclCheck);
		assertTrue(popup.contains("model.addAttribute(\"documentInfo\""));
		assertTrue(popup.contains(
			"documentInfo.put(\"fileCount\", mainFileList.size() + subFileList.size())"));
		assertTrue(popup.contains("model.addAttribute(\"mainDownloadAllowed\""));
		assertTrue(popup.contains("model.addAttribute(\"subDownloadAllowed\""));
		assertTrue(popup.contains("SecurityAclService.DOWNLOAD_ORIGINAL"));
		assertTrue(popup.contains("markPopupDownloadAccess"));
		assertTrue(controller.contains("checkAccessForDisplay"));
	}

	@Test
	void detailQueryReturnsOnlySupportedMetadataAndRequestDate() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/general/distribution/swrequest/SwRequest.xml");
		String detail = section(
			mapper, "<select id=\"selectSwDetailInfo\"", "<select id=\"selectSubFileInfo\"");

		assertTrue(detail.contains("AS \"swNm\""));
		assertTrue(detail.contains("AS \"classificationPath\""));
		assertTrue(detail.contains("AS \"insertUserNm\""));
		assertTrue(detail.contains(
			"TO_CHAR(sw.INSERT_DT, 'YYYY-MM-DD HH24:MI'), '') AS \"insertDt\""));
		assertTrue(detail.contains("FROM DOCS_SW_FILE countedMain"));
		assertTrue(detail.contains("FROM DOCS_SW_SUB_FILE countedSub"));
		assertTrue(detail.contains("COALESCE(countedSub.USE_YN, 'Y') = 'Y'"));

		String[] removedAliases = {
			"swTypeNm",
			"revNo",
			"swVersionNo",
			"businessTypeNm",
			"distributeTypeNm",
			"businessAreaNm",
			"createDt",
			"ccbDate",
			"updateUserNm",
			"updateDt",
			"interfaceDt",
			"stdGappDt",
			"changeGappDt",
			"ecnUserNm",
			"ecnNo",
			"validTypeNm",
			"reviewerUser",
			"approver",
			"status",
			"processingStatusNm",
			"protectYnNm"
		};
		for (String alias : removedAliases) {
			assertFalse(detail.contains("AS \"" + alias + "\""), alias);
		}
		assertFalse(detail.contains("FILE_PATH_NM"));
		assertFalse(detail.contains("CHECKSUM"));
	}

	@Test
	void popupShowsRequestDateAndOmitsUnsupportedFields() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp");
		String detailPanel = section(
			page, "<section class=\"sw-detail-panel\"",
			"<div class=\"section popupCard sectionBlock mainFileSection\">");

		assertTrue(page.contains("code=\"feature.techDetail.title\" text=\"기술자료 상세\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.documentInfo.title\" text=\"문서 정보\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.registrationInfo.title\" text=\"의뢰·등록 정보\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.transmittalNo\" text=\"자료번호\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.requestName\" text=\"의뢰명\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.classification\" text=\"자료분류\""));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.requestDate\" text=\"의뢰일자\""));
		assertTrue(detailPanel.contains("documentInfo.insertDt"));
		assertTrue(detailPanel.contains("code=\"feature.techDetail.registrant\" text=\"등록자\""));
		assertFalse(detailPanel.contains("<dt>등록팀</dt>"));
		assertFalse(detailPanel.contains("documentInfo.insertDeptNm"));
		assertTrue(detailPanel.contains("sw-detail-item sw-detail-item--span-2"));
		assertTrue(page.contains("id=\"swPopupDocumentGrade\""));
		assertTrue(page.contains("<c:if test=\"${mainDownloadAllowed}\">"));
		assertTrue(page.contains("<c:if test=\"${subDownloadAllowed}\">"));
		assertTrue(page.contains("rowdata.downloadAllowed"));

		String[] removedLabels = {
			"CCB번호",
			"CCB제목",
			"SW분류",
			"Revision",
			"SW버전",
			"사업단계",
			"파일유형",
			"사업장",
			"생성일",
			"CCB개최일",
			"수정자",
			"수정일",
			"Interface일",
			"규격화승인일",
			"기술변경승인일",
			"CO담당자",
			"CO번호",
			"유효본",
			"참여자",
			"문서등급",
			"진행상태",
			"처리상태",
			"방산기술",
			"승인자",
			"기종"
		};
		for (String label : removedLabels) {
			assertFalse(detailPanel.contains("<dt>" + label + "</dt>"), label);
		}

		assertTrue(page.contains("grid-template-columns: repeat(4, minmax(0, 1fr))"));
		assertTrue(page.contains("@media (max-width: 900px)"));
		assertTrue(page.contains("@media (max-width: 600px)"));
		assertFalse(detailPanel.contains("objectId"));
		assertFalse(detailPanel.contains("filePath"));
		assertFalse(detailPanel.contains("checkSum"));
		assertFalse(page.contains("CCB번호"));
		assertFalse(page.contains("CCB제목"));
		assertFalse(page.contains("보안·승인 정보"));
		assertFalse(page.contains("documentInfo.status"));
		assertFalse(page.contains("documentInfo.approver"));
		assertFalse(detailPanel.contains("documentInfo.productNm"));
	}

	@Test
	void gridMetadataRemovesUnsupportedColumnsAndLabelsInsertDateAsRequestDate() throws Exception {
		String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

		assertTrue(ddl.contains("DELETE FROM docs_grid_info"));
		assertTrue(ddl.contains("grid_id = 'gridSwRequestList'"));
		assertTrue(ddl.contains("'swTypeNm'"));
		assertTrue(ddl.contains("'revNo'"));
		assertTrue(ddl.contains("'swVersionNo'"));
		assertTrue(ddl.contains("'reviewerUser'"));
		assertTrue(ddl.contains("column_nm = '의뢰일자'"));
		assertTrue(ddl.contains("column_id = 'insertDt'"));
	}

	@Test
	void popupContainsNoApprovalProcedureUiOrRequests() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp");

		assertFalse(page.contains("gridSwApproverStatus"));
		assertFalse(page.contains("gridSwReviewerStatus"));
		assertFalse(page.contains("approveStatusRows"));
		assertFalse(page.contains("saveApprovalComment"));
		assertFalse(page.contains("approval-comment"));
	}

	@Test
	void popupRenderingIsDisplayOnlyButDownloadEndpointKeepsFinalAuthorization() throws Exception {
		String controller = read(
			"src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");

		assertTrue(controller.contains("checkAccessForDisplay(access)"));
		assertTrue(controller.contains("private void requireDownloadAccess"));
		assertTrue(controller.contains("securityAclService.requireAccess(access)"));
		assertTrue(controller.contains("requireDownloadAccess(fileInfo, \"SW\", objectId, fileNo)"));
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
