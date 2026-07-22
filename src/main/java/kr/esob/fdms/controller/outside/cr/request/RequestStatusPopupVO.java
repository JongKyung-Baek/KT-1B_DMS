package kr.esob.fdms.controller.outside.cr.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupVO {
	private String crNo;				// cr번호
	private String vendorUserNm;		// 업체담당자
	private String approvalUser;		// 결재자
	private String businessAreaNm;		// 사업장
	private String purchaserUserNm;		// 구매담당자
	private String drawingNo;			// 도번
	private String crTypeCd;			// 제의사유
	private String productNm;			// 기종
	private String materialNo;			// 자재코드
	private String drawingNm;			// 도명
	private String revNo;				// rev
	private String partNo;				// 규격화 품번
	private String insertDt;		// 도면 등록일
	private String asIsDesc;			//
	private String toBeDesc;			//
	private String crTitleNm;
	private String approvalUserCd;
	private String statusCd;
	private String crStatusCd;
	private String rejectDesc;
	private String deviceNm;


}
