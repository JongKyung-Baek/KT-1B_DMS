package kr.esob.tdms.commonlogic.validTermOver;

import kr.esob.tdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidTermOverListVO {
	private String requestNo;
	private String objectId;
	private String fileNo;
	private String useEndYmd;
	private StatusYn sendEmailYn = StatusYn.Y;
}
