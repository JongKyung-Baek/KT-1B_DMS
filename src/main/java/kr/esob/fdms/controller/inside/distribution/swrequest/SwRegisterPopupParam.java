package kr.esob.fdms.controller.inside.distribution.swrequest;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.inside.distribution.docrequest.DocRegisterPopupParam;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwRegisterPopupParam extends CommonParam {
    private String cnSerial;
    private String objectId;		//자료번호

    private String fileNo;
    private String fileNm;			//파일명 ( 유저가 명명한 이름값 들어감 )
    private String fileSize;		//파일 크기
    private String businessAreaCd;  // 1210 그거
    private String businessTypeCd;  // 개발,양산  이 Cd가지고 판단해서 보여주는거 같음
    private String swNo;
    private String swNm; // 실제 보이는 유저가 명명한 이름
    private String swVersion;
    private String docVersion;
    private String treeCd;
    private String distributeTypeCd; // 원도, 승인도 .. 이 Cd가지고 판단해서 보여준느거 같음
    private String statusCd;
    private String currentPageNo;
    private String totalPageNo;
    private String filePath;
    private String orgFileNm; // 원본 파일 이름
    private String protectYn;
    private String insertDt;
    private String revNo;
    private String swType; // SW분류
    private String ccbIssueDt;
    private String approver;
    private String reviewerUser;


    private List<DocRegisterPopupParam> paramList;

    @Override
    public String toString() {
        return "SwRegisterPopupParam{" +
                ", objectId='" + objectId + '\'' +
                ", fileNm='" + fileNm + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", businessAreaCd='" + businessAreaCd + '\'' +
                ", businessAreaCd='" + businessTypeCd + '\'' +
                ", drawingNo='" + swNo + '\'' +
                ", drawingNm='" + swNm + '\'' +
                ", distributionTypeCd='" + distributeTypeCd + '\'' +
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



