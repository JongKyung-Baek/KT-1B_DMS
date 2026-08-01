package kr.esob.tdms.controller.general.distribution.disposalacceptance;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalAcceptanceParam extends CommonParam {

	private String destroyRequestNo;
	private String rejectRequestDesc;
	private String objectType;
	private String saveFlag;
	private String rejectDesc;

}
