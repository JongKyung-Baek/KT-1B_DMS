package kr.esob.tdms.controller.general.organizationmanage.insidedept;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DeptPopupParam extends CommonParam {
    // 내부 부서 등록/수정
    private String deptCd; // 부서
    private String deptNm;
    private String useYn;
    private String delYn;
    private String sortSeq;
}
