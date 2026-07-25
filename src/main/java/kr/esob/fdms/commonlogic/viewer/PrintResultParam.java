package kr.esob.fdms.commonlogic.viewer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintResultParam {
    private String printJobId;
    private String statusCd;
    private Integer pageCount;
    private Integer copyCount;
    private String printerNm;
    private String deviceId;
    private String errorMessage;
}
