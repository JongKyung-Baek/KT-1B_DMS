package kr.esob.fdms.commonlogic.validTermOver;

import kr.esob.fdms.commonlogic.value.StatusYn;
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
