package kr.esob.tdms.commonlogic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.context.WebApplicationContext;

import kr.esob.tdms.commonlogic.value.RootAbsolutePath;

class CommonMessageContainerTest {

    @TempDir
    Path tempDirectory;

    @Test
    void packagedIndonesianBundleIsLoadedBeforeDatabaseOverlay()
            throws Exception {
        CommonMessageDao messageDao = mock(CommonMessageDao.class);
        CommonMessageVO databaseTranslation = new CommonMessageVO();
        databaseTranslation.setLangType("id");
        databaseTranslation.setLangCd("shared.key");
        databaseTranslation.setLangDesc("Nilai database");
        when(messageDao.selectMessageList()).thenReturn(
                Collections.singletonList(databaseTranslation));

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath("/"))
                .thenReturn(tempDirectory.toString());
        when(servletContext.getResourceAsStream(
                "/messages/message_id.properties"))
                .thenReturn(new ByteArrayInputStream((
                        "packaged.only=Nilai paket\n"
                        + "shared.key=Nilai paket lama\n")
                        .getBytes(StandardCharsets.UTF_8)));
        WebApplicationContext applicationContext =
                mock(WebApplicationContext.class);
        when(applicationContext.getServletContext())
                .thenReturn(servletContext);

        CommonMessageContainer container = new CommonMessageContainer();
        container.commonMessageDao = messageDao;
        container.rootAbsolutePath = new RootAbsolutePath();
        container.webApplicationContext = applicationContext;

        container.init();

        Properties generated = new Properties();
        try (Reader reader = Files.newBufferedReader(
                tempDirectory.resolve("messages/message_id.properties"),
                StandardCharsets.UTF_8)) {
            generated.load(reader);
        }
        assertEquals("Nilai paket", generated.getProperty("packaged.only"));
        assertEquals("Nilai database", generated.getProperty("shared.key"));
    }
}
