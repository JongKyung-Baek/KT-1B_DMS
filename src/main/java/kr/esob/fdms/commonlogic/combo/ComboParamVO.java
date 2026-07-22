package kr.esob.fdms.commonlogic.combo;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComboParamVO extends CommonParam {
	String queryId;
	String comboCd;
}
