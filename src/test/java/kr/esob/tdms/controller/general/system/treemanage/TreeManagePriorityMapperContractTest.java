package kr.esob.tdms.controller.general.system.treemanage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class TreeManagePriorityMapperContractTest {

	private static final String MAPPER =
		"src/main/resources/sqlMaps/oracle/its/controller/general/system/treemanage/SystemTreeManage.xml";

	@Test
	void parentAndChildListsUsePriorityThenStableNameAndCodeOrdering() throws Exception {
		String mapper = read();
		String parent = section(mapper,
			"<select id=\"selectBoardFunctionCode1List\"",
			"<select id=\"selectBoardFunctionCode2List\"");
		String child = section(mapper,
			"<select id=\"selectBoardFunctionCode2List\"",
			"<select id=\"selectBoardDocumentTypeList\"");

		assertTrue(parent.contains("SELECT deduplicated.*"));
		assertTrue(parent.contains(
			"ORDER BY COALESCE(deduplicated.sort, 2147483647),"));
		assertTrue(child.contains(
			"ORDER BY COALESCE(SORT_ORDER, 2147483647), TREE_NM, TREE_CD"));
	}

	@Test
	void mapperRemainsParseableByMyBatis() throws Exception {
		Configuration configuration = new Configuration();
		try (InputStream input = Files.newInputStream(Paths.get(MAPPER))) {
			new XMLMapperBuilder(input, configuration, MAPPER,
				configuration.getSqlFragments()).parse();
		}
		assertTrue(configuration.hasStatement(
			"sql.SystemTreeManage.selectBoardFunctionCode1List"));
		assertTrue(configuration.hasStatement(
			"sql.SystemTreeManage.updateBoardDxfNode"));
	}

	@Test
	void boardInsertsAndUpdatesPersistPriorityAcrossAllThreeTrees() throws Exception {
		String mapper = read();
		for (String table : new String[] {
			"DOCS_SW_TREE", "DOCS_PRODUCT_TREE", "DOCS_DXF_TREE"
		}) {
			String insert = section(mapper,
				"INSERT INTO " + table,
				"</update>");
			assertTrue(insert.contains("#{sortOrder,jdbcType=INTEGER}"),
				"Insert priority is missing for " + table);

			String update = section(mapper,
				"UPDATE " + table,
				"</update>");
			assertTrue(update.contains(
				"SORT_ORDER = COALESCE(#{sortOrder,jdbcType=INTEGER}, SORT_ORDER)"),
				"Update priority is missing for " + table);
		}
	}

	private String section(String source, String startMarker, String endMarker) {
		int start = source.indexOf(startMarker);
		assertTrue(start >= 0, "Start marker not found: " + startMarker);
		int end = source.indexOf(endMarker, start + startMarker.length());
		assertTrue(end >= 0, "End marker not found: " + endMarker);
		return source.substring(start, end);
	}

	private String read() throws Exception {
		return new String(Files.readAllBytes(Paths.get(MAPPER)), StandardCharsets.UTF_8);
	}
}
