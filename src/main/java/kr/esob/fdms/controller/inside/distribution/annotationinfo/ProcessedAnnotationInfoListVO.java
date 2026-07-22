package kr.esob.fdms.controller.inside.distribution.annotationinfo;

import kr.esob.fdms.commonlogic.value.StatusYn;

import java.util.List;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessedAnnotationInfoListVO {

    private String requestNo;		// 요청번호
    private String objectType;




    //	2023.07.04 기범 annotation
    private String objectId;
    private String data;
    private String username;
    private String timestamp;
    private String fontsize;
    private String color;
    private String pageNumber;
    private String xyPercentage;
    private String xyList;



}
