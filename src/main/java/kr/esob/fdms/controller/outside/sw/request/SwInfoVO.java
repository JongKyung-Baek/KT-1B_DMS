package kr.esob.fdms.controller.outside.sw.request;

import java.util.Date;
import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwInfoVO {
	private StatusYn inspectionYn;

	private int cnSerial;
	private String objectId;
	private String businessAreaCd;
	private String businessTypeCd;
	private String swClassCd1;
	private String swClassCd2;
	private String swTypeCd;
	private String productCd;
	private String ecnNo;
	private String ecnInsertUid;
	private String swNo;
	private String revNo;
	private String swNm;
	private String statusCd;
	private String swVersionNo;
	private String docVersionNo;
	private String projectCd;
	private String projectNm;
	private String outputYn;
	private String distributeTypeCd;
	private Date stdGappDt;
	private Date changeGappDt;
	private String securityTypeCd;
	private String protectYn;
	private String insertDeptNm;
	private String insertUid;
	private Date insertDt;
	private String updateDeptNm;
	private String updateUid;
	private Date updateDt;
	private String errcod;
	private String errmsg;
	
	private String fileNm;
	private int fileNo;

	//배포이력 일괄검색에서만 사용되는 sw rev
	private String swRev;
	List<SwInfoVO> list;

	public String getSwTypeNm() {
		return ComboLang.getComboLang("swTypeCd", this.swTypeCd);
	}

	public String getSwInsertDt() {
		return DateUtil.getDateTimeString(this.insertDt);
	}

	public String getSwUpdateDt() {
		return DateUtil.getDateTimeString(this.updateDt);
	}

}
