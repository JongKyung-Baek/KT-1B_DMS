package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRegisterPageLayoutContractTest {

	@Test
	void fullPageRegistrationOverridesLegacyContainerScrolling() throws Exception {
		String css = read(
			"src/main/resources/static/css/pages/technical-data-register.css");
		String page = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swRegisterPage.jsp");

		assertTrue(css.contains("height: auto !important;"));
		assertTrue(css.contains("min-height: 0 !important;"));
		assertTrue(css.contains("overflow: visible !important;"));
		assertTrue(page.contains("technical-data-register.css?v=20260801.1"));
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}
