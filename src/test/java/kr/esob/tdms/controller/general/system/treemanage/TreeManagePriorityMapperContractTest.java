package kr.esob.tdms.controller.general.system.treemanage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class TreeManagePriorityMapperContractTest {

	private static final String MAPPER =
		"src/main/resources/sqlMaps/oracle/its/controller/general/system/treemanage/SystemTreeManage.xml";
	private static final Path RUNTIME_MAPPERS = Paths.get("src/main/resources/sqlMaps");
	private static final Path PRODUCTION_MAPPER = RUNTIME_MAPPERS.resolve(
		"oracle/its/controller/general/distribution/production/DistributionProductionRequest.xml");
	private static final Path DXF_MAPPER = RUNTIME_MAPPERS.resolve(
		"oracle/its/controller/general/distribution/dxf/DxfRequest.xml");

	@Test
	void parentAndChildListsUsePriorityThenStableNameAndCodeOrdering() throws Exception {
		String mapper = read();
		String parent = section(mapper,
			"<select id=\"selectBoardFunctionCode1List\"",
			"<select id=\"selectBoardFunctionCode2List\"");
		String child = section(mapper,
			"<select id=\"selectBoardFunctionCode2List\"",
			"<select id=\"selectBoardDocumentTypeList\"");

		assertTrue(parent.contains("FROM DOCS_SW_TREE"));
		assertTrue(parent.contains(
			"ORDER BY COALESCE(SORT_ORDER, 2147483647), TREE_NM, TREE_CD"));
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
			"sql.SystemTreeManage.updateBoardSwNode"));
		assertFalse(configuration.hasStatement(
			"sql.SystemTreeManage.updateBoardProductNode"));
		assertFalse(configuration.hasStatement(
			"sql.SystemTreeManage.updateBoardDxfNode"));
	}

	@Test
	void boardInsertsAndUpdatesPersistPriorityOnlyInCanonicalTree() throws Exception {
		String mapper = read();
		String insert = section(mapper, "INSERT INTO DOCS_SW_TREE", "</update>");
		assertTrue(insert.contains("#{sortOrder,jdbcType=INTEGER}"));

		String update = section(mapper, "UPDATE DOCS_SW_TREE", "</update>");
		assertTrue(update.contains(
			"SORT_ORDER = COALESCE(#{sortOrder,jdbcType=INTEGER}, SORT_ORDER)"));
	}

	@Test
	void runtimeMappersHaveNoDuplicateTechnicalTreeReferences() throws Exception {
		String allMappers;
		try (java.util.stream.Stream<Path> paths = Files.walk(RUNTIME_MAPPERS)) {
			allMappers = paths
				.filter(path -> path.toString().endsWith(".xml"))
				.sorted()
				.map(path -> {
					try {
						return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
					} catch (java.io.IOException e) {
						throw new java.io.UncheckedIOException(e);
					}
				})
				.collect(Collectors.joining("\n"))
				.toUpperCase(Locale.ROOT);
		}

		assertFalse(allMappers.contains("DOCS_PRODUCT_TREE"));
		assertFalse(allMappers.contains("DOCS_DXF_TREE"));
	}

	@Test
	void dormantProductAndDxfRoutesBrowseTheCanonicalTechnicalTree() throws Exception {
		for (Path path : new Path[] { PRODUCTION_MAPPER, DXF_MAPPER }) {
			String mapper = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
				.toUpperCase(Locale.ROOT);
			assertTrue(mapper.contains("FROM DOCS_SW_TREE TREE"), path.toString());
			assertTrue(mapper.contains("FROM DOCS_SW_TREE NODE"), path.toString());
			assertTrue(mapper.contains("JOIN DOCS_SW_TREE SELECTEDNODE"), path.toString());
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
