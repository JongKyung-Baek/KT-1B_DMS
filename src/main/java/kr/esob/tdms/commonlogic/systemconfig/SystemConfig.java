package kr.esob.tdms.commonlogic.systemconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import kr.esob.tdms.commonlogic.value.Constant;

public class SystemConfig {
	private static volatile Map<String, String> systemConfig = Collections.emptyMap();

	private SystemConfig() {
	}

	public static String getSystemConfigValue(String configCode) {
		if (configCode == null) {
			return "";
		}
		String value = systemConfig.get(Constant.SYSTEM_CONFIG + "|" + configCode);
		return value == null ? "" : value;
	}

	/**
	 * Atomically publishes a defensive, immutable copy of the application-wide
	 * system configuration.
	 */
	public static void replaceSystemConfig(Map<String, String> values) {
		if (values == null || values.isEmpty()) {
			systemConfig = Collections.emptyMap();
			return;
		}
		systemConfig = Collections.unmodifiableMap(
				new HashMap<String, String>(values));
	}

	public static Map<String, String> snapshot() {
		return systemConfig;
	}

}
