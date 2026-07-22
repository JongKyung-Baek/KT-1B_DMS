package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsDefensePursUserVO extends CommonParam {
	private String userId;
	private String userNm;
}
