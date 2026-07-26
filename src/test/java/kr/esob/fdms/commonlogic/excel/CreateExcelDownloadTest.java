package kr.esob.fdms.commonlogic.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import kr.esob.fdms.commonlogic.value.RootAbsolutePath;

class CreateExcelDownloadTest {

	@TempDir
	Path tempDirectory;

	@Test
	void generatedExcelUrlUsesAuthenticatedControllerInsteadOfStaticWebRoot() {
		CreateExcelService service = serviceWithRoot(tempDirectory);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContextPath("/SDMS-KT-1B");

		String url = service.buildDownloadUrl(request, "excel/grid_202607261300001.xlsx");

		assertEquals(
				"/SDMS-KT-1B/common/createExcel/download/grid_202607261300001.xlsx",
				url);
	}

	@Test
	void resolverAcceptsOnlyGeneratedExcelNamesInsideExcelDirectory() {
		CreateExcelService service = serviceWithRoot(tempDirectory);

		assertEquals(
				tempDirectory.resolve("excel/grid_202607261300001.xlsx")
						.toAbsolutePath()
						.normalize(),
				service.resolveGeneratedExcelFile("grid_202607261300001.xlsx"));
		assertNull(service.resolveGeneratedExcelFile("../grid_202607261300001.xlsx"));
		assertNull(service.resolveGeneratedExcelFile("technical-data.xlsx"));
	}

	@Test
	void downloadStreamsGeneratedExcelFromRuntimeWebRoot() throws Exception {
		String fileName = "grid_202607261300001.xlsx";
		Path generatedFile = tempDirectory.resolve(fileName);
		byte[] contents = "xlsx-test".getBytes(StandardCharsets.UTF_8);
		Files.write(generatedFile, contents);

		CreateExcelService service = mock(CreateExcelService.class);
		when(service.resolveGeneratedExcelFile(fileName)).thenReturn(generatedFile);
		CreateExcelController controller = new CreateExcelController();
		ReflectionTestUtils.setField(controller, "service", service);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(get("/common/createExcel/download/{fileName}", fileName))
				.andExpect(status().isOk())
				.andExpect(content().contentType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.andExpect(header().string(
						HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + fileName + "\""))
				.andExpect(content().bytes(contents));
	}

	@Test
	void downloadReturnsNotFoundForUnknownGeneratedFile() throws Exception {
		CreateExcelService service = mock(CreateExcelService.class);
		when(service.resolveGeneratedExcelFile("grid_202607261399999.xlsx")).thenReturn(null);
		CreateExcelController controller = new CreateExcelController();
		ReflectionTestUtils.setField(controller, "service", service);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(get(
						"/common/createExcel/download/{fileName}",
						"grid_202607261399999.xlsx"))
				.andExpect(status().isNotFound());
	}

	private CreateExcelService serviceWithRoot(Path root) {
		RootAbsolutePath rootAbsolutePath = new RootAbsolutePath();
		rootAbsolutePath.setRootAbsolutePath(root.toAbsolutePath().toString());
		CreateExcelService service = new CreateExcelService();
		ReflectionTestUtils.setField(service, "rootAbsolutePath", rootAbsolutePath);
		return service;
	}
}
