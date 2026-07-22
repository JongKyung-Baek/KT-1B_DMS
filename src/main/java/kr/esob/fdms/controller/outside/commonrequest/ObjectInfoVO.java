package kr.esob.fdms.controller.outside.commonrequest;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ObjectInfoVO {

	private String objectType;
	private String objectId;
	private String businessAreaCd;
	private String businessTypeCd;
	private String objectClassCd1;
	private String objectClassCd2;
	private String objectClassNm1;
	private String objectClassNm2;
	private String swClassNm1;
	private String swClassNm2;
	private String docClassNm1;
	private String docClassNm2;
	private String swTypeCd;
	private String drawingType;
	private String drawingRev;
	private int currentPageNo;
	private int totalPageNo;
	private Date effectiveDt;
	private String productCd;
	private String ecnNo;
	private String ecnInsertUid;
	private String objectNo;
	private String revNo;
	private String objectNm;
	private String accountNm;
	private String purchaserUid;
	private String statusCd;
	private int totalPageCnt;
	private int totalSheet;
	private String swVersionNo;
	private String docVersionNo;
	private String projectCd;
	private String projectNm;
	private Date stdGappDt;
	private Date changeGappDt;
	private String description;
	private String securityTypeCd;
	private String applyProductNo;
	private String detailProductNo;
	private String applyHogiNo;
	private String callId;
	private String processTypeCd;
	private String protectYn;
	private String insertDeptNm;
	private String insertUid;
	private Date insertDt;
	private String updateDeptNm;
	private String updateUid;
	private Date updateDt;
	private Date interfaceDt;
	private String plantCd;
	private String vendorCd;
	private String fileNo;
	private String fileViewNm;
	private String orgFileNm;
	private String fileNm;
	private String filePathNm;
	private String checkSum;
	private String distributeTypeCd;
	private Long fileSize;
	private StatusYn inspectionYn;
	private int cnSerial;
	private String objectTypeNm;
	private String useStartYmd;
	private String useEndYmd;

	public String getDistributeTypeNm() {
		return ComboLang.getComboLang("distributeTypeCd", this.distributeTypeCd);
	}

	public String getBusinessTypeNm() {
		return ComboLang.getComboLang("businessTypeCd", this.businessTypeCd);
	}

	public String getCrDrawingInsertDt() {
		return DateUtil.getDateString(this.insertDt);
	}
	public String getDrawingUpdateDt() {
		return DateUtil.getDateString(this.updateDt);
	}
	public String getDocUpdateDt() {
		return DateUtil.getDateString(this.updateDt);
	}
	public String getSwUpdateDt() {
		return DateUtil.getDateString(this.updateDt);
	}
	public String getInterfaceDt() {
		return DateUtil.getDateString(this.interfaceDt);
	}
	
	public String getStdGappDt() {
		return DateUtil.getDateString(this.stdGappDt);
	}
	
	public String getChangeGappDt() {
		return DateUtil.getDateString(this.changeGappDt);
	}

	public String getDocClassCdNm() {
		return ComboLang.getComboLang("docClassCd1", this.objectClassCd1);
	}

	List<ObjectInfoVO> list;

}
