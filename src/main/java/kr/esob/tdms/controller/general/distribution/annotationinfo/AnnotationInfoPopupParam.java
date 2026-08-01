package kr.esob.tdms.controller.general.distribution.annotationinfo;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationInfoPopupParam extends CommonParam {
    private String requestNo;		// 요청번호
    private String objectType;




    //	2023.07.04 기범 annotation
    private String objectId;
    private String data;
    private String username;
    private String timestamp;

}