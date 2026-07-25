package kr.esob.fdms.controller.inside.cr;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrFileDownloadParam extends CommonParam {
    private String crNo;
    private int fileNo;
}
