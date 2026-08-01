package kr.esob.tdms.commonlogic.loginhistory;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryParam extends CommonParam {
	private Long historySeq;
	private String accessIp;
	private String loginType;
}
