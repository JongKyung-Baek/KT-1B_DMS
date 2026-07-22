package kr.esob.fdms.controller.inside.production.distributionstatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionStatusListParam extends CommonParam {
	private String businessAreaCd;		// 사업장
	private String deployStartDt;		// 배포일자
	private String deployEndDt;

	public String getDeployStartDt() {
		if(null == deployStartDt) {
			return "";
		}

		return this.deployStartDt.replace("-", "");
	}

	public String getDeployEndDt() {
		if(null == deployEndDt) {
			return "";
		}

		return this.deployEndDt.replace("-", "");
	}
}