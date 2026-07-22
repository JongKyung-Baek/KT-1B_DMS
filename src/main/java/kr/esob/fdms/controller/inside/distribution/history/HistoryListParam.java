package kr.esob.fdms.controller.inside.distribution.history;

import java.util.List;
import java.util.Map;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.outside.doc.request.DocInfoVO;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import kr.esob.fdms.controller.outside.sw.request.SwInfoVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String requestNo;		    // 요청번호
	private String deployCompanyCd;		// 업체명
	private String objectNo;			// 자료번호
	private String validTermOverYn;		// 유효기간경과
	private String businessAreaCd;		// 사업장
	private String purchaserUserCd;		// 구매담당자
	private String statusCd;			// 승인상태
	private String productCd;			// 기종
	private String objectType;			// 자료구분
	private String destroyStatusCd;		// 폐기상태
	private String businessTypeCd;		// 사업구분
	private String distributeTypeCd;	// 배포유형
	private String deployType;			// 배포방식
	private String approvalStartDt;		// 승인시작일
	private String approvalEndDt;		// 승인종료일
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String approvalUserCd;		// 결재자
	private String searchAllParam;
	private List<DrawingInfoVO> drawingList;
	private List<DocInfoVO> documentList;
	private List<SwInfoVO> swList;
	private String approvalDt;
//	private String[] drawingNoList;
//	private String[] drawingRevList;
//	private String[] documentNoList;
//	private String[] documentRevList;
//	private String[] swNoList;
//	private String[] swRevList;
	private String useLike;

//	public List<Map<String, String>> getDrawingList() {
//		for(int i=0; i<=drawingNoList.length; i++) {
//			Map<String, String> drawingInfo = drawingInfo.put("drawingNo", drawingNoList[i]);
//
//		}
//	}

}
