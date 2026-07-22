package kr.esob.fdms.controller.inside.distribution.downhistory;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownListParam extends CommonParam{





    private String requestNo;		        // 요청번호
    private String userNm;                  // 사용자
    private String objectNo;                // 자료번호
    private String objectNm;                // 자료명
    private String downDate;                // 다운로드 일
    private String downCount;               // 다운로드 횟수
    private String objectId;                // objectId
    private String downloadedName;          // downloadedName
    private String actType;
    private String actTime;





    private String objectType;				// 자료유형
    private String requestUserCd;			// 출력요청자
    private String destroyRequestUserCd;	// 폐기요청자
    private String requestPurpose;			// 용도
    private String businessAreaCd;			// 사업장
    private String destroyStatusCd;			// 폐기상태코드
    private String requestStartDt;			// 요청시작일
    private String requestEndDt;			// 요청종료일
    private String businessTypeCd;			// 사업유형










}
