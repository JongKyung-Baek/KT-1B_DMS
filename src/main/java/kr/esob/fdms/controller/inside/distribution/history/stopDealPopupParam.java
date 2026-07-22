package kr.esob.fdms.controller.inside.distribution.history;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class stopDealPopupParam extends CommonParam {
	private String companyCd;			//회사코드
	private String companyNm;			//업체명
	private String bizNo;				//사업자번호
	
	// 폐기 요청을 위한 데이터
	private String requestNo; 			//요청번호
	private String objectId;			//아이템번호
	private String destroyRequestNo;	//폐기 요청 번호
	private String processSeq;
	private String approvalStatusCd;	

	List<stopDealPopupParam> list;
}
