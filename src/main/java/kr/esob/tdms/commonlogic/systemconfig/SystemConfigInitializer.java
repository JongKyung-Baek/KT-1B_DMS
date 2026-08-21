package kr.esob.tdms.commonlogic.systemconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import kr.esob.tdms.commonlogic.value.Constant;

/**
 * Loads application-wide settings before scheduled and background work starts.
 */
@Component
public class SystemConfigInitializer {
    private static final Logger log = LoggerFactory.getLogger(SystemConfigInitializer.class);
    private static final String[][] RUNTIME_OVERRIDES = {
            {"TDMS_FILE_API_BASE_URL", "FILE_API_BASE_URL"},
            {"TDMS_FILE_API_KEY", "FILE_API_KEY"},
            {"TDMS_FILE_API_SW_FOLDER", "FILE_API_SW_FOLDER"}
    };

    private final SystemConfigDao systemConfigDao;
    private final Environment environment;

    public SystemConfigInitializer(SystemConfigDao systemConfigDao,
                                   Environment environment) {
        this.systemConfigDao = systemConfigDao;
        this.environment = environment;
    }

    @PostConstruct
    public void initialize() {
        reload();
    }

    public int reload() {
        List<SystemConfigVO> rows = systemConfigDao.selectSystemConfig();
        Map<String, String> values = new HashMap<String, String>();
        if (rows != null) {
            for (SystemConfigVO row : rows) {
                if (row == null || row.getSystemConfigCd() == null) {
                    continue;
                }
                values.put(Constant.SYSTEM_CONFIG + "|" + row.getSystemConfigCd(),
                        row.getSystemConfigValue());
            }
        }
        // File API credentials are deployment secrets. Runtime values take
        // precedence over legacy database rows so a secret never needs to be
        // copied into a migration, Git artifact, or application log.
        for (String[] override : RUNTIME_OVERRIDES) {
            String value = environment.getProperty(override[0]);
            if (StringUtils.hasText(value)) {
                values.put(Constant.SYSTEM_CONFIG + "|" + override[1], value.trim());
            }
        }
        SystemConfig.replaceSystemConfig(values);
        log.info("System configuration initialized. entryCount={}", values.size());
        return values.size();
    }
}
