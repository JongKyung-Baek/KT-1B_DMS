package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestListPresentationContractTest {

	@Test
	void technicalDataListRemovesIntroCardAndKeepsTheFunctionalGridLayout() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swRequestList.jsp");

		assertTrue(page.contains("/resources/css/pages/technical-data-list.css"));
		assertTrue(page.contains("technical-data-list-page"));
		assertTrue(page.contains("technical-data-results-card"));
		assertTrue(page.contains("code='feature.techList.aria.results' text='기술자료 검색 및 목록'"));
		assertTrue(page.contains("<custom:listTemplateInvoice"));
		assertTrue(page.contains("gridId=\"gridSwRequestList\""));
		assertTrue(page.contains("treeId=\"swRequestExplorerTree\""));
		assertFalse(page.contains("tdl-hero-card"));
		assertFalse(page.contains("technicalDataTreeChip"));
		assertFalse(page.contains("technicalDataResultCount"));
		assertFalse(page.contains("조회 결과 기준"));
	}

	@Test
	void technicalDataListKeepsTreeNavigationWithoutRemovedHeroState() throws Exception {
		String script = read(
			"src/main/resources/static/js/views/inside/distribution/swRequestList.js");

		assertTrue(script.contains("function updateSwRequestTreeSelection"));
		assertTrue(script.contains("renderToolbarNavigator(selectedPath)"));
		assertFalse(script.contains("updateTechnicalDataResultCount"));
		assertFalse(script.contains("technicalDataTreeChip"));
		assertFalse(script.contains("technicalDataResultCount"));
		assertFalse(script.contains("tdl-result-card"));
	}

	@Test
	void technicalDataListKeepsTheGridFlexibleAndResponsive() throws Exception {
		String css = read(
			"src/main/resources/static/css/pages/technical-data-list.css");

		assertTrue(css.contains(".technical-data-list-page > .technical-data-results-card"));
		assertTrue(css.contains(".technical-data-list-page .technical-data-results-card > .distribution-invoice-layout"));
		assertTrue(css.contains("padding: 22px"));
		assertTrue(css.contains("box-shadow: 0 5px 20px rgba(47, 43, 61, 0.055)"));
		assertTrue(css.contains("height: auto;"));
		assertTrue(css.contains("flex: 1 1 auto;"));
		assertTrue(css.contains(".technical-data-list-page .distribution-filter-card"));
		assertTrue(css.contains(".technical-data-list-page .distribution-tree-card"));
		assertTrue(css.contains(".technical-data-list-page .distribution-grid-card"));
		assertTrue(css.contains("#gview_gridSwRequestList .ui-jqgrid-htable th"));
		assertTrue(css.contains("#gview_gridSwRequestList .ui-jqgrid-btable tr.jqgrow td"));
		assertTrue(css.contains("tr[aria-selected=\"true\"] td"));
		assertTrue(css.contains("#gridSwRequestListPager"));
		assertTrue(css.contains("background: #f4f2f6"));
		assertTrue(css.contains("border-bottom: 1px solid #efedf1"));
		assertTrue(css.contains("height: 56px"));
		assertTrue(css.contains("height: 42px"));
		assertTrue(css.contains("min-height: 36px"));
		assertTrue(css.contains("gap: 7px"));
		assertTrue(css.contains("background: var(--tdl-primary)"));
		assertTrue(css.contains("min-width: 28px"));
		assertTrue(css.contains("@media (max-width: 991.98px)"));
		assertTrue(css.contains("@media (max-width: 575.98px)"));
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
