package kr.esob.fdms.commonlogic.value;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ObjectType {
	DOC("DOC", "G"),
	DRAWING("DRAWING", "D"),
	SW("SW", "S"),
	CR("CR", "R"),
	PRODUCT_DOC("PRODUCT_DOC", "P"),
	PRODUCT_SW("PRODUCT_SW", "P");

	private String objectType;
	private String code;
}
