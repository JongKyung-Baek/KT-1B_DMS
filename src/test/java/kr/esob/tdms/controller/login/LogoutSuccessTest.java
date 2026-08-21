package kr.esob.tdms.controller.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.tdms.commonlogic.value.Constant;

class LogoutSuccessTest {

    @AfterEach
    void reset() {
        SystemConfig.replaceSystemConfig(Collections.emptyMap());
    }

    @Test
    void logoutDoesNotClearApplicationWideSystemConfiguration() throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        values.put(Constant.SYSTEM_CONFIG + "|FILE_API_BASE_URL",
                "http://127.0.0.1:18080");
        SystemConfig.replaceSystemConfig(values);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new LogoutSuccess().onLogoutSuccess(
                new MockHttpServletRequest(), response, null);

        assertEquals("http://127.0.0.1:18080",
                SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
        assertEquals("/", response.getRedirectedUrl());
    }
}
