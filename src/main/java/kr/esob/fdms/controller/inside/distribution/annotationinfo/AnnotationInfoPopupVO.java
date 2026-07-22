package kr.esob.fdms.controller.inside.distribution.annotationinfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationInfoPopupVO {


    private String contents;	// 주석 내용
    private String date;		// 작성 날짜
    private String userCode;	// 유저코드
    private String details;		// 자세히 ( 폰트, 글자 색, 주석 위치 등등 )


    private String orgFileNm;	// 뭔지 몰라서 냅둠


    public String getOrgFileNmData() {
        return this.orgFileNm;
    }

}
