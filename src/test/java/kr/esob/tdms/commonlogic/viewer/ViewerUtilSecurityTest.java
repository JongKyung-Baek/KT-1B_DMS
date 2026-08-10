package kr.esob.tdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ViewerUtilSecurityTest {

	@TempDir
	Path tempDirectory;

	@Test
	void secureXmlFactoriesBlockAllExternalResourceAccess() throws Exception {
		DocumentBuilderFactory documentFactory =
				ViewerUtil.newSecureDocumentBuilderFactory();

		assertTrue(documentFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
		assertTrue(documentFactory.getFeature(
				"http://apache.org/xml/features/disallow-doctype-decl"));
		assertFalse(documentFactory.getFeature(
				"http://xml.org/sax/features/external-general-entities"));
		assertFalse(documentFactory.getFeature(
				"http://xml.org/sax/features/external-parameter-entities"));
		assertFalse(documentFactory.getFeature(
				"http://apache.org/xml/features/nonvalidating/load-dtd-grammar"));
		assertFalse(documentFactory.getFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd"));
		assertFalse(documentFactory.isXIncludeAware());
		assertFalse(documentFactory.isExpandEntityReferences());
		assertEquals("", documentFactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
		assertEquals("", documentFactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA));

		TransformerFactory transformerFactory =
				ViewerUtil.newSecureTransformerFactory();
		assertTrue(transformerFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
		assertEquals("", transformerFactory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
		assertEquals("", transformerFactory.getAttribute(
				XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
	}

	@Test
	void validSvgIsStillSplitIntoSeparateFiles() throws Exception {
		Path source = tempDirectory.resolve("drawing.svg");
		Files.write(source, (
				"<document xmlns=\"http://www.w3.org/2000/svg\">"
				+ "<svg id=\"page-one\"><rect width=\"10\" height=\"10\"/></svg>"
				+ "<svg id=\"page-two\"><circle r=\"4\"/></svg>"
				+ "</document>").getBytes(StandardCharsets.UTF_8));

		List<String> outputFiles = ViewerUtil.executeSvgFileParser(source.toString());

		assertEquals(2, outputFiles.size());
		assertEquals(tempDirectory.resolve("drawing_001.svg").toString(), outputFiles.get(0));
		assertEquals(tempDirectory.resolve("drawing_002.svg").toString(), outputFiles.get(1));
		assertTrue(Files.exists(tempDirectory.resolve("drawing_001.svg")));
		assertTrue(Files.exists(tempDirectory.resolve("drawing_002.svg")));
		assertTrue(new String(Files.readAllBytes(tempDirectory.resolve("drawing_001.svg")),
				StandardCharsets.UTF_8).contains("page-one"));
		assertTrue(new String(Files.readAllBytes(tempDirectory.resolve("drawing_002.svg")),
				StandardCharsets.UTF_8).contains("page-two"));
	}

	@Test
	void svgWithExternalEntityIsRejectedWithoutProducingOutput() throws Exception {
		Path secret = tempDirectory.resolve("secret.txt");
		Files.write(secret, "must-not-be-read".getBytes(StandardCharsets.UTF_8));
		Path source = tempDirectory.resolve("malicious.svg");
		String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE document [<!ENTITY xxe SYSTEM \""
				+ secret.toUri() + "\">]>"
				+ "<document><svg><text>&xxe;</text></svg></document>";
		Files.write(source, xml.getBytes(StandardCharsets.UTF_8));

		List<String> outputFiles = ViewerUtil.executeSvgFileParser(source.toString());

		assertTrue(outputFiles.isEmpty());
		assertFalse(Files.exists(tempDirectory.resolve("malicious_001.svg")));
	}
}
