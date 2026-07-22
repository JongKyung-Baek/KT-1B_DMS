package kr.esob.fdms.controller.inside.production.approval;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalPopupVO {
	private String requestNo;			// 요청번호

	private String businessAreaCd;		// 사업장
	private String deptCd;				// 부서코드
	private String deptNm;				// 부서
	private String userNm;				// 성명
	private int deployCount;			// 배포매수
	private int destroyCount;		// 폐기(예정)

	private String copy;				//부수

	private String itemCount;			//매수

	private String productCd;			// 기종
	private String objectTypeNm;		// 자료유형
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String rev;					// rev
	private String objectId;

	private int totalCount;

	private String fileNm;
	private int fileNo;

	private String requestDesc;

	private String userCd;

	private String objectClassCd2;
	private String objectClassNm2;
	private String objectType;


	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}

	public String getObjectTypeNm() {
		return ComboLang.getComboLang("objectType", this.objectTypeNm);
	}


}