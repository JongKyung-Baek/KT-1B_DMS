package kr.esob.fdms.commonlogic.form;


import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormInfoVO {
	private String formId;
	private String columnId;
	private String columnType;
	private String columnName;
	private int columnSort;
	private String columnSize;
	private String columnAlign;
	private String systemClassGroup;
	private StatusYn useYn = StatusYn.N;
	private String messageCode;
	private int maxLength;
	private String callFunc;
	private String defaultText;
	private String minDate;
	private String maxDate;
	private String defaultValue;
	private String formatter;
	private StatusYn detailYn = StatusYn.N;
	private String queryId;
	private String searchUrl;
	private String columnHidden;


	public boolean getUseYn() {
		return useYn.isBooleanValue();
	}

	public boolean getDetailYn() {
		return detailYn.isBooleanValue();
	}


}
