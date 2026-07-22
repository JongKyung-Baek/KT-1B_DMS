package kr.esob.fdms.controller.inside.distribution.docrequest;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocRegisterPopupParam extends CommonParam {

    private String cnSerial;
    private String objectId;
    private String parentObjectId;

    private String fileNm;
    private String fileSize;
    private String businessAreaCd;
    private String businessTypeCd;
    private String docNo;
    private String treeCd;
    private String docNm;
    private String distributeTypeCd;
    private String fileType;
    private String statusCd;
    private String currentPageNo;
    private String totalPageNo;
    private String filePath;
    private String orgFileNm;
    private String protectYn;
    private String insertDt;
    private String revNo;

    private String docClassCd1;
    private String approvalDate;
    private String approver;
    private String coPublisher;
    private String reviewerUser;
    private String useYn;

    private List<DocRegisterPopupParam> paramList;

    @Override
    public String toString() {
        return "DocRegisterPopupParam{" +
                "objectId='" + objectId + '\'' +
                ", fileNm='" + fileNm + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", businessAreaCd='" + businessAreaCd + '\'' +
                ", businessTypeCd='" + businessTypeCd + '\'' +
                ", docNo='" + docNo + '\'' +
                ", docNm='" + docNm + '\'' +
                ", distributeTypeCd='" + distributeTypeCd + '\'' +
                ", statusCd='" + statusCd + '\'' +
                ", currentPageNo='" + currentPageNo + '\'' +
                ", totalPageNo='" + totalPageNo + '\'' +
                ", filePath='" + filePath + '\'' +
                ", orgFileNm='" + orgFileNm + '\'' +
                ", protectYn='" + protectYn + '\'' +
                ", insertDt='" + insertDt + '\'' +
                '}';
    }
}
