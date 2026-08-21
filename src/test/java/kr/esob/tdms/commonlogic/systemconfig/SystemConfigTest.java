package kr.esob.tdms.commonlogic.systemconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import kr.esob.tdms.commonlogic.value.Constant;

class SystemConfigTest {

    @AfterEach
    void reset() {
        SystemConfig.replaceSystemConfig(Collections.emptyMap());
    }

    @Test
    void missingOrUninitializedConfigurationReturnsEmptyValue() {
        SystemConfig.replaceSystemConfig(null);

        assertEquals("", SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
        assertEquals("", SystemConfig.getSystemConfigValue(null));
    }

    @Test
    void replacementIsDefensivelyCopiedAndPublishedAsImmutableSnapshot() {
        Map<String, String> values = new HashMap<String, String>();
        String key = Constant.SYSTEM_CONFIG + "|FILE_API_BASE_URL";
        values.put(key, "http://127.0.0.1:18080");

        SystemConfig.replaceSystemConfig(values);
        values.put(key, "http://changed.invalid");

        assertEquals("http://127.0.0.1:18080",
                SystemConfig.getSystemConfigValue("FILE_API_BASE_URL"));
        assertThrows(UnsupportedOperationException.class,
                () -> SystemConfig.snapshot().put(key, "mutated"));
    }
}
