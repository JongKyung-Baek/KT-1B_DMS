package kr.esob.fdms.controller.inside.distribution.annotationinfo;

import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestParam;

import java.util.List;


import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationRequestParam {
    private String cnSerial;
    private String protectYn;
    private String companyCd;
    private String companyUserCd;
    private String deployUserEmail;
    private String purchaseTeam;
    private String purchaseUid;
    private String protectiveOfficerUid;
    private String distributeMethodCd;				//파일배포 유형 (VIEW, GENERAL, SECURITY)
    private String requestPurpose;
    private String requestPurposeTerm;				//유효기간 (개월)
    private String requestDesc;
    private String requestType;						//파일 유형 (DOC, DRAWING, SW)
    private String approvalRequestType;				//요청 유형 (배포, 출력)
    private String approvalLineId;
    private String currentProcessSeqNo;
    private String useStartYmd;
    private String useEndYmd;
    private String printYn;							//뷰/출력 여부
    private String deployNormalYn;
    private String deploySpecialYn;
    private String objectType;
    private String requestNo;
    private String requestKeyType;
    private String processSeq;
    private String approvalStatusCd;
    private String approvalGradeCd;
    private String actionCd;
    private String objectId;
    private String businessTypeCd;					//개발/양산
    private int fileNo;
    private String filePath;
    private String orgFileNm;
    private String fileViewNm;
    private long fileSize;
    private String fileNm;
    private String checkSum;
    private StatusYn sendEmailYn = StatusYn.Y;

    private String fileDistributionType;			//파일 배포 방식(general, security)
    private String destroyTodoYmd;					//폐기 예정 일자

    private String deployTerm;						//유효기간(개월)

    private String businessAreaCd;					//사업장


    private String productProtectYn;				//개발 방산보호
    private String developmentProtectYn;			//양산 방산보호

    private String developmentProtectUserUid;		//개발 결재자
    private String productProtectUid;				//양산 결재자

    private String distributeTypeCd;				//배포 유형

    private List<String> protectUserUid;
    private List<AnnotationRequestParam> list;





}
