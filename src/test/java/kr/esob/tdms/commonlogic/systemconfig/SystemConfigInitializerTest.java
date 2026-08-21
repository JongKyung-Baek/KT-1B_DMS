package kr.esob.tdms.commonlogic.systemconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SystemConfigInitializerTest {

    @AfterEach
    void reset() {
        SystemConfig.replaceSystemConfig(Collections.emptyMap());
    }

    @Test
    void startupInitializationLoadsDatabaseSettingsWithoutLoginSession() {
        SystemConfigDao dao = mock(SystemConfigDao.class);
        SystemConfigVO fileApi = config("FILE_API_BASE_URL", "http://127.0.0.1:18080");
        SystemConfigVO ignored = config(null, "ignored");
        when(dao.selectSystemConfig()).thenReturn(Arrays.asList(fileApi, null, ignored));

        int count = new SystemConfigInitializer(dao, new MockEnvironment()).reload();

        assertEquals(1, count);
        assertEquals("http://127.0.0.1:18080",
                SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
    }

    @Test
    void runtimeFileApiSettingsOverrideDatabaseWithoutLoggingOrPersistence() {
        SystemConfigDao dao = mock(SystemConfigDao.class);
        when(dao.selectSystemConfig()).thenReturn(Arrays.asList(
                config("FILE_API_BASE_URL", "https://legacy.invalid"),
                config("FILE_API_KEY", "legacy-key"),
                config("FILE_API_SW_FOLDER", "LEGACY")));
        MockEnvironment environment = new MockEnvironment()
                .withProperty("TDMS_FILE_API_BASE_URL", "http://127.0.0.1:18080")
                .withProperty("TDMS_FILE_API_KEY", "runtime-secret-value")
                .withProperty("TDMS_FILE_API_SW_FOLDER", "UPLOAD");

        int count = new SystemConfigInitializer(dao, environment).reload();

        assertEquals(3, count);
        assertEquals("http://127.0.0.1:18080",
                SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
        assertEquals("runtime-secret-value",
                SystemConfig.getSystemConfigValue("FILE_API_KEY"));
        assertEquals("UPLOAD",
                SystemConfig.getSystemConfigValue("FILE_API_SW_FOLDER"));
    }

    private SystemConfigVO config(String code, String value) {
        SystemConfigVO config = new SystemConfigVO();
        config.setSystemConfigCd(code);
        config.setSystemConfigValue(value);
        return config;
    }
}
