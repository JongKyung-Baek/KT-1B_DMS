package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawingSubFileParam extends CommonParam {
    private String objectId;
    private String parentObjectId;
    private Integer fileNo;
    private String orgFileNm;
    private String filePath;
    private String fileSize;
    private String useYn;
}
