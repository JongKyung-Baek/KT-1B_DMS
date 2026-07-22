package kr.esob.fdms.controller.outside.duanzong.pdm;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdmListVO {
	private String pno;							/* NO */ 
	private String updateDt;         			/* 최종등록(갱신)일자 */
	private String prodCdNm;                    /* 기종 */
	private String businessAreaCd;            	/* 사업장 */
	private String deptNm;                      /* 부서 */
	private String deliveryVendorNm;        	/* 납품엄체명 */
	private String deliveryLruPartNo;    		/* 납춤조립체부품번호(URL)*/
	
	private String deliveryPartNm; 				/* 납품 조립체 품명 */
	private String hyperstasisSruPartNo;		/* 차상위 조립체 부품번호(SRU) */
	private String hyperstasisPartNm;			/* 차상위 조립체 품명 */
	private String makeNm;						/* 원제조사(국명) */
	private String makePartNo;					/* 원제조사 부품번호 */
	private String standardizationPartNo;		/* 규격화 부품번호(제안된 공급품 도면) */
	private String partNm;						/* 품명 */
	private String classification;				/* 분류 */
	private String manageDiv;					/* 관리구분 */
	private String contractPartNo;				/* 원제조사 부품번호(필요시 보정) */
	private String matchedMfr;					/* Matched Mfr */
	private String matchedPn;					/* Matched P/N */
	private String matchedPartDesc;				/* Matched Part Description */
	private String partCategory;				/* Part Category */
	private String currentPartSt;				/* 현재 Part Status */
	private String pastPartNo;					/* 이전 Part Status */
	private float lccCd;						/* LCC(Life Cycle Code : 수명상태 추치 값) */
	private String lcsCd;						/* LCS(Life Cycle Stage : 수명 상태) */
	private String estimatiedYteol;				/* Estimated YTEOL */
	private String ltbDt;						/* LTB Date(Last Time Buy (마지막 구매 가능 일자)) */
	private String ltdDt;						/* LTD Date(Last Time Delivery (마지막 배송 가능 일자)) */
	private String recommedSubstitutedVendor;	/* 추천 대체품 제조사 */
	private String recommedSubstitutedPartNo;	/* 추천 대체품 부품번호 */
	private String acquireDt;					/* 획득일자 */
	private String skillChange;					/* 기술변경 */
	private String duanzongSynthesisYn;			/* 단종여부 (종합결정) */
	private String duanzongYn;					/* 단종여부 */
	private String duanzongDt;					/* 단종 예정일 */
	private String duanzongReason;				/* 단종 사유 */
	private String duanzongCause;				/* 단종 근거 */
	private int stock;							/* 재고 (추가 조달 가능수 포함) */
	private String productionActionDt;			/* 생산대응 가능월 */
	private String daunzongMeasure;				/* 단종대책 */
	private String contractInfor;				/* 원제조사 연락처 (전화, 이메일) */
	private String replacementPartNo;			/* 대체품 제조사 품번 (제조사명) */
	private String costImpact;					/* 원가영향성 */
	private String acquisitionDt;				/* 획득일자 */
	private String makePartNo1;					/* 제조사품번1(제조사명) */
	private String makePartNo2;					/* 제조사품번2(제조사명) */
	private String makePartNo3;					/* 제조사품번3(제조사명) */
	private String makePartNo4;					/* 제조사품번4(제조사명) */
	private String makePartNo5;					/* 제조사품번5(제조사명) */
	private String insertUid;					/* 등록/수정자 계정 */
	private String insertNm;					/* 등록/수정자 */
	private String insertDt;					/* 등록/수정일자 */
	
	public String getUpdateDt() {
		return DateUtil.getYmd(updateDt);
	}
	
	public String getLtbDt() {
		return DateUtil.getYmd(ltbDt);
	}
	
	public String getLtdDt() {
		return DateUtil.getYmd(ltdDt);
	}
	
	public String getAcquireDt() {
		return DateUtil.getYmd(acquireDt);
	}
	
	public String getDuanzongDt() {
		return DateUtil.getYmd(duanzongDt);
	}
	
	public String getProductionActionDt() {
		return DateUtil.getYmd(productionActionDt);
	}
	
	public String getAcquisitionDt() {
		return DateUtil.getYmd(acquisitionDt);
	}
	
	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}
}