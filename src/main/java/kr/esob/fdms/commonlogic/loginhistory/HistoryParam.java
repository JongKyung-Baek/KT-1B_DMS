package kr.esob.fdms.commonlogic.loginhistory;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryParam extends CommonParam {
	private Long historySeq;
	private String accessIp;
	private String loginType;
}
