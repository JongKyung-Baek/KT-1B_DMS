package kr.esob.fdms.controller.inside.distribution.video;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VideoInfoVO {
    private StatusYn inspectionYn;
    private int cnSerial;
    private String objectId;
    private String businessAreaCd;
    private String businessTypeCd;
    private String productCd;
    private String ecnNo;
    private String ecnInsertUid;
    private String videoNo;
    private String videoNm;
    private String revNo;
    private String distributeTypeCd;
    private String statusCd;
    private int currentPageNo;
    private int totalPageNo;
    private Date effectiveDt;
    private String filePathNm;
    private String orgFileNm;
    private String fileNm;
    private long fileSize;
    private String useTypeCd;
    private Date stdGappDt;
    private Date changeGappDt;
    private String materialsCd;
    private String partNo;
    private String partNm;
    private String stdPartNo;
    private String purchaserUserCd;
    private String vendorCd;
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
    private String rowId;
    private String companyNm;

    private String videoRev;

    public String getDistributeTypeNm() {
        return ComboLang.getComboLang("distributeTypeCd", this.distributeTypeCd);
    }

    public String getBusinessTypeNm() {
        return ComboLang.getComboLang("businessTypeCd", this.businessTypeCd);
    }

    public String getCrVideoInsertDt() {
        return DateUtil.getDateTimeString(this.insertDt);
    }
    public String getVideoUpdateDt() {
        return DateUtil.getDateTimeString(this.updateDt);
    }

    List<VideoInfoVO> list;

}


