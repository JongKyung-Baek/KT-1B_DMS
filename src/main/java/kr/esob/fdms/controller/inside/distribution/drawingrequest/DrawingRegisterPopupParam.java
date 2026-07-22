package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DrawingRegisterPopupParam extends CommonParam {



    private String cnSerial;
    private String objectId;		//자료번호
    private String objectType;		//자료 유형 (DOC, DRAWING, SW)
    private String fileNm;			//파일명 ( 유저가 명명한 이름값 들어감 )
    private String fileSize;		//파일 크기
    private String businessAreaCd;  // 1210 ,1310
    private String businessTypeCd;  // 개발,양산  이 Cd가지고 판단해서 보여주는거 같음
    private String drawingNo;
    private String treeCd;
    private String approvalDate;
    private String approver;
    private String status;
    private String coPublisher;
    private String reviewerUser;
    private String drawingNm; // 실제 보이는 유저가 명명한 이름
    private String distributeTypeCd; // 원도, 승인도 .. 이 Cd가지고 판단해서 보여준느거 같음
    private String fileType;
    private String statusCd;
    private String currentPageNo;
    private String totalPageNo;
    private String filePath;
    private String orgFileNm; // 원본 파일 이름
    private String protectYn;
    private String insertDt;
    private String drawingType; // 도면 종류( 2D,3D )
    private String revNo;
    private String distributionPoint;   // 배포처
    private String modelCode;   // 기종
    private String customerRevision;   // 고객 리비전


    private List<DrawingRegisterPopupParam> paramList;

    @Override
    public String toString() {
        return "DrawingRegisterPopupParam{" +
                ", objectId='" + objectId + '\'' +
                ", objectType='" + objectType + '\'' +
                ", fileNm='" + fileNm + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", businessAreaCd='" + businessAreaCd + '\'' +
                ", businessAreaCd='" + businessTypeCd + '\'' +
                ", drawingNo='" + drawingNo + '\'' +
                ", drawingNm='" + drawingNm + '\'' +
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



