package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class CommonApprovalPopupListVO {

	private String businessTypeCd;			//사업구분
	private String distributeTypeCd;		//배포유형
	private String distributionMethodCd;	// 배포방식
	private String objectType;				//자료유형
	private String distributeType;			//배포방식
	private String objectNo;				//자료번호
	private String rev;						//REV
	private String swVersion;				//S/W버전
	private String docVersionNo;			//명세서버전
	private String objectNm;				//자료명
	private String page;					//페이지
	private String currentPage;				//현재페이지
	private String totalPage;				//총페이지
	private String stdGappDt;				//규격화승인일
	private String changeGappDt;			//기술변경승인일
	private String productNo;				//기종
	private String businessAreaCd;			//사업장구분
	private String protectYn;				//방산기술
	private String objectId;				//아이템id
	private String requestNo;
	private String objectTypeCd;
	private String businessArea;
	private String fileNo;
	private String orgFileNm;
	private String useStartYmd;
	private String useEndYmd;
	private String fileNm;
	private String deployUserCd;
	private String deployCompanyCd;

	public String getBusinessTypeCd() {
		return ComboLang.getComboLang("businessTypeCd", this.businessTypeCd);
	}

	public String getDistributeTypeCd() {
		return ComboLang.getComboLang("distributeTypeCd", this.distributeTypeCd);
	}

	public String getObjectType() {
		return ComboLang.getComboLang("objectType", this.objectType);
	}

	public String getBusinessAreaCd() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}

	public String getDistributionMethodCd() {
		return ComboLang.getComboLang("distributeMethodCd", this.distributionMethodCd);
	}

	public String getUseEndYmd() throws ParseException {						//개월 수 차이 구하기
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
		if(!(null==this.useStartYmd) && !(null==this.useEndYmd)) {
			Date start = transFormat.parse(this.useStartYmd);
			Date end = transFormat.parse(this.useEndYmd);
			return DateUtil.getDiffMonth(start, end);
		} else {
			return this.useEndYmd;

		}
	}
}
