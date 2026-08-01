package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestItemSnapshot {
    private Long itemId;
    private Long requestId;
    private int lineNo;
    private int documentLineNo;
    private int fileLineNo;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String materialNo;
    private String materialName;
    private String originalFileName;
    private Long fileSize;
    private String gradeCd;
    private String gradeNm;
    private String treeCd;
    private String treeNm;
    private String parentTreeCd;
    private String parentTreeNm;
    private String snapshotAt;
}
