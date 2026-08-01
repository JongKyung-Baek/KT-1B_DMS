package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestTreeExplorerContractTest {

	private static final String[] TREE_MESSAGE_KEYS = {
		"feature.techList.tree.title",
		"feature.techList.tree.description",
		"feature.techList.tree.searchPlaceholder",
		"feature.techList.tree.allDocuments",
		"feature.techList.tree.count",
		"feature.techList.tree.noMatches",
		"feature.techList.tree.selection",
		"feature.techList.tree.clearSelection",
		"feature.techList.tree.category.drawing",
		"feature.techList.tree.category.spec",
		"feature.techList.tree.category.sow",
		"feature.techList.tree.category.sdrl",
		"feature.techList.tree.category.programData",
		"feature.techList.tree.category.sro",
		"feature.techList.tree.category.testProcedure",
		"feature.techList.tree.category.engineeringMemo",
		"feature.techList.tree.category.sourceData",
		"feature.techList.tree.category.etc",
		"feature.techList.tree.category.mfgData"
	};

	@Test
	void treeEndpointOverwritesAclIdentityWithTheAuthenticatedUser() throws Exception {
		String controller = read(
			"src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");
		String endpoint = section(
			controller, "@RequestMapping(\"/selectTree\")", "@RequestMapping(\"/nextSwNo\")");

		int requireCurrentUser = endpoint.indexOf(
			"securityAclService.requireCurrentUser()");
		int bindAclIdentity = endpoint.indexOf(
			"param.setAclUserCd(currentUser.getUserCd())");
		int selectTree = endpoint.indexOf("service.selectTree(param)");

		assertTrue(requireCurrentUser >= 0,
			"The tree endpoint must require an authenticated user.");
		assertTrue(bindAclIdentity > requireCurrentUser,
			"The authenticated user must overwrite any client supplied ACL identity.");
		assertTrue(selectTree > bindAclIdentity,
			"The ACL identity must be bound before the tree query is executed.");
	}

	@Test
	void treeMapperCountsOnlyAclAccessibleDocumentsForEveryAncestor() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/general/distribution/swrequest/SwRequest.xml");
		String treeQuery = section(
			mapper, "<select id=\"selectTree\"", "<select id=\"selectList\"");
		String treeVo = read(
			"src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestTreeVO.java");

		assertTrue(treeQuery.contains("WITH RECURSIVE active_tree AS"));
		assertTrue(treeQuery.contains("tree_descendants (ancestor_cd, descendant_cd) AS"));
		assertTrue(treeQuery.contains("accessible_documents AS"));
		assertTrue(treeQuery.contains("<include refid=\"accessibleSwListAcl\"/>"));
		assertTrue(treeQuery.contains("tree_document_counts AS"));
		assertTrue(treeQuery.contains("COUNT(DISTINCT documentInfo.OBJECT_ID)"));
		assertTrue(treeQuery.contains("COALESCE(documentCount.document_count, 0) AS \"documentCount\""));
		assertTrue(treeVo.contains("private Integer documentCount;"));
	}

	@Test
	void technicalDataTreeProvidesSearchAllCountAndSelectionPathUi() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp");
		String tag = read(
			"src/main/webapp/WEB-INF/tags/listTemplateInvoice.tag");
		String script = read(
			"src/main/resources/static/js/views/general/distribution/swRequestList.js");
		String css = read(
			"src/main/resources/static/css/pages/technical-data-list.css");

		assertTrue(page.contains("treeSearchPlaceholder=\"${treeSearchPlaceholder}\""));
		assertTrue(page.contains("treeAllLabel=\"${treeAllLabel}\""));
		assertTrue(page.contains("\"feature.techList.tree.count\""));
		assertTrue(page.contains("\"feature.techList.tree.selection\""));

		assertTrue(tag.contains("class=\"technical-tree-search-input\""));
		assertTrue(tag.contains("id=\"${treeId}SearchClear\""));
		assertTrue(tag.contains("id=\"${treeId}All\""));
		assertTrue(tag.contains("id=\"${treeId}AllCount\""));
		assertTrue(tag.contains("id=\"${treeId}Total\""));
		assertTrue(tag.contains("class=\"technical-tree-no-results\""));

		assertTrue(script.contains("function bindSwRequestTreeExplorerControls()"));
		assertTrue(script.contains("function applySwRequestTreeSearch(value)"));
		assertTrue(script.contains("function getSwRequestTreeDocumentCount(node)"));
		assertTrue(script.contains("\"class\": \"tree-node-count\""));
		assertTrue(script.contains("function renderToolbarNavigator(pathLabel)"));
		assertTrue(script.contains("\"class\": \"tree-toolbar-navigator-path\""));

		assertTrue(css.contains(
			".technical-data-list-page .technical-tree-search"));
		assertTrue(css.contains(
			".technical-data-list-page .technical-tree-all"));
		assertTrue(css.contains(
			".technical-data-list-page .tree-node-count"));
		assertTrue(css.contains(
			".technical-data-list-page .tree-toolbar-navigator .tree-toolbar-navigator-path"));
	}

	@Test
	void koreanAndEnglishBundlesContainEveryTreeExplorerMessage() throws Exception {
		String korean = read("src/main/webapp/messages/feature.properties");
		String english = read("src/main/webapp/messages/feature_en.properties");

		assertMessageKeys(korean, "Korean");
		assertMessageKeys(english, "English");
	}

	private void assertMessageKeys(String bundle, String language) {
		for (String key : TREE_MESSAGE_KEYS) {
			assertTrue(bundle.contains(key + "="),
				language + " bundle is missing message key: " + key);
		}
	}

	private String section(String source, String startMarker, String endMarker) {
		int start = source.indexOf(startMarker);
		assertTrue(start >= 0, "Start marker not found: " + startMarker);
		int end = source.indexOf(endMarker, start + startMarker.length());
		assertTrue(end >= 0, "End marker not found: " + endMarker);
		return source.substring(start, end);
	}

	private String read(String path) throws Exception {
		return new String(
			Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
