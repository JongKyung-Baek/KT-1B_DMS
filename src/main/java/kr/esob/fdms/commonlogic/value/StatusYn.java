package kr.esob.fdms.commonlogic.value;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusYn {
	Y("1", true),
	N("0", false);

	private String binaryValue;
	private boolean booleanValue;


}
