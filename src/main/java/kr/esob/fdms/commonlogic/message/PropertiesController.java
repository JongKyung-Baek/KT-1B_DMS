package kr.esob.fdms.commonlogic.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.fdms.commonlogic.value.RootAbsolutePath;

@Controller
public class PropertiesController {
	@Inject
	RootAbsolutePath rootAbsolutePath;

	@RequestMapping("/messages/{propertiesName}")
    public void getProperties(@PathVariable String propertiesName, HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        response.setContentType("text/plain; charset=UTF-8");
        
        //보안 취약성 조치에 따라 필터링 처리
        propertiesName = propertiesName.replace("/", "");
        propertiesName = propertiesName.replace("\\", "");
        propertiesName = propertiesName.replace(".", "");
        propertiesName = propertiesName.replace("&", "");
        propertiesName = propertiesName.replace("properties", ".properties");
        
        File messageFile = new File(rootAbsolutePath+"messages/"+propertiesName);
        InputStream inputStream = new FileInputStream(messageFile);

        List<String> readLines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        IOUtils.writeLines(readLines, null, outputStream, StandardCharsets.UTF_8);

        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
    }
}
