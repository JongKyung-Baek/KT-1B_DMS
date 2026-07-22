package kr.esob.fdms.controller.inside.distribution.video;


import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoRequestParam extends CommonParam {
    private String businessTypeCd;
    private String distributeTypeCd;
    private String videoNo;
    private String videoNm;
    private String insertUid;
    private String ecnNo;
    private String companyCd;
    private String purchaserUid;
    private String productNo;
    private String businessAreaCd;
    private String insertStartDt;
    private String insertEndDt;
    private String interfaceStartDt;
    private String interfaceEndDt;
    private String productCd;
    private String [] specialCondition;
    private String searchAllParam;
    private List<VideoInfoVO> videoList;
    private String useLike;
    private String validType;

    public String getVendorYn() {
        if(null == specialCondition) {
            return "N";
        }
        for(int i=0; i<specialCondition.length; i++) {
            if(specialCondition[i].equals("vendor")) {
                return "Y";
            }
        }

        return "N";
    }

    public String getLastRevisionYn() {
        if(null == specialCondition) {
            return "N";
        }
        for(int i=0; i<specialCondition.length; i++) {
            if(specialCondition[i].equals("lastRevision")) {
                return "Y";
            }
        }

        return "N";
    }
}
