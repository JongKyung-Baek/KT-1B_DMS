package kr.esob.fdms.commonlogic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletResponse;

import kr.esob.fdms.commonlogic.value.RootAbsolutePath;

class PropertiesControllerTest {

    @TempDir
    java.nio.file.Path tempDirectory;

    private PropertiesController controller;
    private ServletContext servletContext;

    @BeforeEach
    void setUp() {
        controller = new PropertiesController();
        RootAbsolutePath rootAbsolutePath = new RootAbsolutePath();
        rootAbsolutePath.setRootAbsolutePath(tempDirectory.toString());
        controller.rootAbsolutePath = rootAbsolutePath;
        servletContext = mock(ServletContext.class);
        controller.servletContext = servletContext;
    }

    @Test
    void bundledFeaturePropertiesAreServedFromExecutableWar()
            throws Exception {
        String content = "feature.common.count={0} records\n";
        when(servletContext.getResourceAsStream(
                "/messages/feature_en.properties"))
                .thenReturn(new ByteArrayInputStream(
                        content.getBytes(StandardCharsets.UTF_8)));
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getProperties("feature_en.properties", response);

        assertEquals(200, response.getStatus());
        assertEquals(content, response.getContentAsString(
                StandardCharsets.UTF_8));
    }

    @Test
    void invalidPropertyFilenameIsRejected() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getProperties("../secret.properties", response);

        assertEquals(400, response.getStatus());
        verify(servletContext, never()).getResourceAsStream(anyString());
    }

    @Test
    void missingBundleReturnsNotFoundInsteadOfServerError()
            throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getProperties("missing.properties", response);

        assertEquals(404, response.getStatus());
    }
}
