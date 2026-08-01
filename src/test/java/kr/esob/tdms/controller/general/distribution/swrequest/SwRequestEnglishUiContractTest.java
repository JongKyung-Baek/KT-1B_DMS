package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestEnglishUiContractTest {

	@Test
	void pageInjectsRequestLocaleMessagesBeforeTheCacheBustedTreeScript()
			throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp");
		int messages = page.indexOf("window.SdmsPageMessages = {");
		int script = page.indexOf("swRequestList.js?v=20260726.4");

		assertTrue(messages >= 0);
		assertTrue(script > messages);
		assertTrue(page.contains("\"feature.techList.tree.root\""));
		assertTrue(page.contains("\"feature.techList.tree.toggle\""));
		assertTrue(page.contains("\"feature.documentGrade.general\""));
		assertTrue(page.contains("\"feature.techList.grade.label\""));
		assertTrue(page.contains("\"feature.grid.pager.rowsPerPage\""));
		assertTrue(page.contains("\"feature.grid.pager.total\""));
	}

	@Test
	void treePrefersServerResolvedPageMessagesToKoreanFallbacks()
			throws Exception {
		String script = read(
			"src/main/resources/static/js/views/general/distribution/swRequestList.js");

		assertTrue(script.contains("window.SdmsPageMessages"));
		assertTrue(script.contains(
			"Object.prototype.hasOwnProperty.call(window.SdmsPageMessages, key)"));
		assertTrue(script.contains(
			"swRequestMessage(\"feature.techList.tree.root\", \"기술자료\")"));
		assertTrue(script.contains(
			"swRequestMessage(\"feature.techList.tree.toggle\", \"하위 분류 열기/닫기\")"));
	}

	@Test
	void gradeFormatterLocalizesKnownDatabaseNamesAndAccessibilityText()
			throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp");

		assertTrue(page.contains("case \"일반\":"));
		assertTrue(page.contains("case \"GENERAL\":"));
		assertTrue(page.contains(
			"swRequestMessage(\"feature.documentGrade.general\", \"일반\")"));
		assertTrue(page.contains(
			"swRequestMessage(\"feature.techList.grade.label\", \"문서등급\")"));
		assertTrue(page.contains("+ '\" aria-label=\"' +"));
	}

	@Test
	void customGridPagerUsesLocaleMessagesInsteadOfKoreanConcatenation()
			throws Exception {
		String paging = read(
			"src/main/resources/static/js/common_grid_paging.js");

		assertTrue(paging.contains("function gridPagingMessage"));
		assertTrue(paging.contains("\"feature.grid.pager.rowsPerPage\""));
		assertTrue(paging.contains("\"feature.grid.pager.total\""));
		assertTrue(paging.contains("\"feature.grid.pager.goToPage\""));
		assertFalse(paging.contains("\"총\" + commify(totalSize)"));
		assertFalse(paging.contains("+ \"건 표시\" +"));
	}

	private String read(String path) throws Exception {
		return new String(
			Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
