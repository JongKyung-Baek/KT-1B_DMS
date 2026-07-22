package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsDefensePursVO extends CommonParam {
	private String comboVal;
	private String comboLabel;
	private String combocd;
	private String combotooltip;
	private String defaultText;
}
