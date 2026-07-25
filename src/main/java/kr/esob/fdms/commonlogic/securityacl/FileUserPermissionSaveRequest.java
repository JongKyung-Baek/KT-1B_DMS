package kr.esob.fdms.commonlogic.securityacl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUserPermissionSaveRequest {
    private String objectType;
    private String objectId;
    private String fileNo;
    private String changeReason;
    private List<FileUserPermissionVO> permissions;
}
