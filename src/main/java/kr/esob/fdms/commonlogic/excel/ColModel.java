package kr.esob.fdms.commonlogic.excel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColModel {
	private String align;
	private String columnId;
	private boolean editable;
	private boolean hidden;
	private String label;
	private int maxlength;
	private String name;
	private int width;

}
