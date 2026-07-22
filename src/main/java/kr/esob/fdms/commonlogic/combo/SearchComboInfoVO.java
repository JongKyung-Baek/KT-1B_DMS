package kr.esob.fdms.commonlogic.combo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchComboInfoVO {
	private String id;
	private String value;
	private String text;
	private String title;
	private String tooltip;
	private String pageindex;
	private boolean selected;

}
