package kr.esob.fdms.controller.outside.outregisted.requeststatus;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupVO {
	private String requestNo;			// 요청번호
	private String deployCompanyNm;		// 배포업체
	private String deployUserNm;		// 배포 받을 사용자
	private String deployEmail;			// 배포 받을 이메일
	private String receiveUserNm;		// 수신자
	
	private String distributeMethodNm;	// 배포방식
	private String requestPurpose;		// 용도
	private String requestUserNm;		// 요청자
	private String requestDt;			// 요청일
	private String useEndYmd;			// 유효기간
	private String businessAreaCd;		// 사업장
	private String statusCd;			// 상태
	
	private String transDesc;			// 자료전송 사유
	private String deployTerm;			// 유효 기간 (개월)
	
	//미등록 배포 요청 아이템 리스트 데이터
	private String objectNm;			// 자료명
	private String objectType;			// 자료 유형
	private String fileNm;				// 파일명
	private String objectId;			
	private String orgFileNm;
	
	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}
	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}
	public String getStatusNm() {
		return ComboLang.getComboLang("requestStatusCd", this.statusCd);
	}
	public String getObjectType() {
		return ComboLang.getComboLang("objectType", this.objectType);
	}
	
	public String getRequestPurpose() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}
}
