package kr.esob.tdms.commonlogic.systemconfig;

import java.util.Map;

import kr.esob.tdms.commonlogic.value.Constant;

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
