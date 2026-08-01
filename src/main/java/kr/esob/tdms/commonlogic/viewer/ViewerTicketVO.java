package kr.esob.tdms.commonlogic.viewer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewerTicketVO {
    private String disposableKey;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String fileName;
    private String userCd;
    private String sessionId;
}
