package kr.esob.fdms.commonlogic.systemconfig;

import java.util.Map;

import kr.esob.fdms.commonlogic.value.Constant;

public class SystemConfig {
	public static Map<String, String> systemConfig;

	public static String getSystemConfigValue(String configCode) {
		if(!systemConfig.isEmpty() && systemConfig.containsKey(Constant.SYSTEM_CONFIG + "|" + configCode)) {
			return systemConfig.get(Constant.SYSTEM_CONFIG + "|" + configCode);
		}else {
			return "";
		}
	}

}
