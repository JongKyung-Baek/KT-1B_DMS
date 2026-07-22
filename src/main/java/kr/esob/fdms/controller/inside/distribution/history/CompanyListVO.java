package kr.esob.fdms.controller.inside.distribution.history;

import java.util.List;

import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyListVO {
	private String companyCd;			// 회사코드
	private String companyNm;			// 업체명
	private String bizNo;			   	        // 사업자번호
	private String stopDeal;				    // 거래중단
	
	private StatusYn sendEmailYn = StatusYn.Y;

	private List<CompanyListVO> list;
}
