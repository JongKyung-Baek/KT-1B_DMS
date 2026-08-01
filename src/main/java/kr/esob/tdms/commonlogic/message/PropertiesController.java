package kr.esob.tdms.commonlogic.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.tdms.commonlogic.value.RootAbsolutePath;

@Controller
public class PropertiesController {
    private static final Pattern PROPERTIES_FILE =
            Pattern.compile("^[A-Za-z0-9_-]+\\.properties$");

    @Inject
    RootAbsolutePath rootAbsolutePath;

    @Inject
    ServletContext servletContext;

    @RequestMapping("/messages/{propertiesName}")
    public void getProperties(
            @PathVariable String propertiesName,
            HttpServletResponse response) throws IOException {
        if (propertiesName == null
                || !PROPERTIES_FILE.matcher(propertiesName).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        InputStream inputStream = openGeneratedMessage(propertiesName);
        if (inputStream == null) {
            // Executable WARs do not expose bundled web resources as regular
            // files. ServletContext can still stream them from the archive.
            inputStream = servletContext.getResourceAsStream(
                    "/messages/" + propertiesName);
        }
        if (inputStream == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("text/plain; charset=UTF-8");
        try (InputStream source = inputStream;
                OutputStream target = response.getOutputStream()) {
            IOUtils.copy(source, target);
        }
    }

    private InputStream openGeneratedMessage(String propertiesName)
            throws IOException {
        String webRoot = rootAbsolutePath.getRootAbsolutePath();
        if (webRoot == null || webRoot.trim().isEmpty()) {
            return null;
        }
        File messageFile = new File(
                new File(webRoot, "messages"), propertiesName);
        return messageFile.isFile()
                ? new FileInputStream(messageFile)
                : null;
    }
}
