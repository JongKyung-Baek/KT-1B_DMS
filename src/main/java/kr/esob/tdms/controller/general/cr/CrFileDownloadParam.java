package kr.esob.tdms.controller.general.cr;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrFileDownloadParam extends CommonParam {
    private String crNo;
    private int fileNo;
}
