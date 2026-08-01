package kr.esob.tdms.controller.general.organizationmanage.insidedept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class DeptListVO {
    private String deptCd;
    private String deptNm;
    private String useYn;
    private String delYn;
}
