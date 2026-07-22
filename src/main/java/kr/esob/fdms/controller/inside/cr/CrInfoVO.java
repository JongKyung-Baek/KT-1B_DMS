package kr.esob.fdms.controller.inside.cr;

import java.util.Date;
import java.util.List;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrInfoVO {
	private int cnSerial;
	private String crNo;
	private String businessAreaCd;
	private String businessTypeCd;
	private String productNo;
	private String drawingNo;
	private String revNo;
	private String drawingNm;
	private String crTitleNm;
	private String materialNo;
	private String partNo;
	private String purchaserUid;
	private String purchaserUserCd;
	private String crTypeCd;
	private Date drawingInsertDt;
	private String asIsDesc;
	private String toBeDesc;
	private String vendorNm;
	private String vendorNo;
	private String vendorUid;
	private String vendorUserCd;
	private String vendorEmailNm;
	private String statusCd;
	private Date receiptDt;
	private String insertUid;
	private Date insertDt;
	private String errcod;
	private String errmsg;
	private String filePathNm;
	private String approvalUser;
	private String reviewResult;
	private String rejectReason;
	private String productCd;
	private String deviceNm;

	private String objectId;
	private List<CrFileVO> fileList;

	public String getCrTypeDesc() {
		return ComboLang.getComboLang("crTypeCd", this.crTypeCd);
	}
}
