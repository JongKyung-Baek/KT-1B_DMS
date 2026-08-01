package kr.esob.tdms.controller.general.distribution.workflow;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/** One selectable technical-data document and its complete file bundle. */
@Getter
@Setter
public class DistributionDocumentBundle {
    private String objectId;
    private String materialNo;
    private String materialName;
    private String treeCd;
    private String treeNm;
    private String parentTreeCd;
    private String parentTreeNm;
    private int mainFileCount;
    private int subFileCount;
    private int totalFileCount;
    private List<DistributionRequestItemSnapshot> files =
        new ArrayList<DistributionRequestItemSnapshot>();
}
