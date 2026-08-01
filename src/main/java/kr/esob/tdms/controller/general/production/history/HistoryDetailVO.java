package kr.esob.tdms.controller.general.production.history;

import java.util.Date;
import java.util.List;

import kr.esob.tdms.commonlogic.value.StatusYn;
import kr.esob.tdms.controller.general.production.common.ProductionInfoVO;
import kr.esob.tdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDetailVO {
	private String requestNo;
	private Date requestDt;
	private String requestUserCd;
	private String requestUserNm;
	private String approvalUser;
	private String requestPurpose;
	private String requestDesc;
	private String useEndYmd;
	private StatusYn watermarkYn;
	private List<ProductionInfoVO> productionInfoVoList;
	private List<RequestDetailVO> requestDetailVoList;
	private List<RequestDeployVO> requestDeployVoList;

	public String getRequestDt() {
		return DateUtil.getDateString(this.requestDt);
	}

}
