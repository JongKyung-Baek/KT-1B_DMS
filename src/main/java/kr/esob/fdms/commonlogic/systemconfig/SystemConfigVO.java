package kr.esob.fdms.commonlogic.systemconfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemConfigVO {
	private String systemConfigGroup;
	private String systemConfigCd;
	private String systemConfigValue;
	private String systemConfigDesc;
//	private Map<String, String> system

}
