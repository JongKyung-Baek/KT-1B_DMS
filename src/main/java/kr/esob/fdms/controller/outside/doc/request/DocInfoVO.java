package kr.esob.fdms.controller.outside.doc.request;

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
public class DocInfoVO {
	private StatusYn inspectionYn;

	private int cnSerial;
	private String objectId;
	private String businessAreaCd;
	private String businessTypeCd;
	private String docClassCd1;
	private String docClassCd2;
	private String productCd;
	private String ecnNo;
	private String ecnInsertUid;
	private String documentNo;
	private String revNo;
	private String documentNm;
	private String accountNm;
	private String statusCd;
	private String projectCd;
	private String projectNm;
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
	private String distributeTypeCd;
	
	private String fileNm;
	private int fileNo;

	//배포이력 일괄검색에서만 사용되는 document rev
	private String documentRev;

	List<DocInfoVO> list;

	public String getDocClassCdNm() {
		return ComboLang.getComboLang("docClassCd1", this.docClassCd1);
	}

	public String getDocInsertDt() {
		return DateUtil.getDateTimeString(this.insertDt);
	}

	public String getDocUpdateDt() {
		return DateUtil.getDateTimeString(this.updateDt);
	}

}
