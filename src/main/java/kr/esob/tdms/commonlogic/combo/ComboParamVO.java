package kr.esob.tdms.commonlogic.combo;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComboParamVO extends CommonParam {
	String queryId;
	String comboCd;
}
