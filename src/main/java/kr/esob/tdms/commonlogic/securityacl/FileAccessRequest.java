package kr.esob.tdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAccessRequest {
    private String actionCd;
    private String objectType;
    private String permissionObjectType;
    private String objectId;
    private String fileNo;
    private String requestNo;

    // The service always overwrites this value from SecurityContext.
    private String actorUserCd;
}
