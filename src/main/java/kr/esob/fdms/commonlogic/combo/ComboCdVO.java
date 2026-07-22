package kr.esob.fdms.commonlogic.combo;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComboCdVO {
	private String comboCd;
	private int seq;
	private String value;
	private String langCd;
	private String text;
	private String tooltip;
	private String langDesc;

	private Map<String, String> langMap;


}
