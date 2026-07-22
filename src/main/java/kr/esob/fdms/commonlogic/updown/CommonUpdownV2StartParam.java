package kr.esob.fdms.commonlogic.updown;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommonUpdownV2StartParam {
    private String wsSeq;
    private String reqType;
    private String requestNo;
    private String docSeq;
    private String dataNo;
    private String objectType;
    private String fileNo;
    private String fileSeq; 
    private String fileNm;
    private String orgFileNm;
    private String fileExt;
}
