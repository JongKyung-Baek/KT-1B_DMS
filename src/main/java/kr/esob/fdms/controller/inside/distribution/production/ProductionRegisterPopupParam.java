package kr.esob.fdms.controller.inside.distribution.production;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRegisterPopupParam extends CommonParam {
    private String cnSerial;
    private String objectId;		//자료번호

    private String fileNo;
    private String fileNm;			//파일명 ( 유저가 명명한 이름값 들어감 )
    private String fileSize;		//파일 크기
    private String businessAreaCd;  // 1210 그거
    private String businessTypeCd;  // 개발,양산  이 Cd가지고 판단해서 보여주는거 같음
    private String treeCd;
    private String productionNo;
    private String productionNm; // 실제 보이는 유저가 명명한 이름
    private String docVersion;
    private String distributeTypeCd; // 원도, 승인도 .. 이 Cd가지고 판단해서 보여준느거 같음
    private String statusCd;
    private String currentPageNo;
    private String totalPageNo;
    private String filePath;
    private String orgFileNm; // 원본 파일 이름
    private String protectYn;
    private String insertDt;
    private String revNo;
    private String registerNo;
    private String ccbIssueDt;

    private String pdm; // pdm매수

    private String distributionPoint;   // 배포처
    private String modelCode;   // 기종
    private String customerRevision;   // 고객 리비전
    private String changeActionNo;   // CA 번호
    private String check3DFile;   // 3D 여부
    private String approver;
    private String reviewerUser;


    private List<ProductionRegisterPopupParam> paramList;

}
