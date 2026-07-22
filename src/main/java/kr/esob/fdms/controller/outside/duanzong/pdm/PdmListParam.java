package kr.esob.fdms.controller.outside.duanzong.pdm;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdmListParam extends CommonParam {
	private String delivery_vendor_nm; 			/* 납품업체명 */		
	private String hyperstasis_sru_part_no; 	/* 차상위조립체 부품번호(SRU) */
	private String delivery_lru_part_no; 		/* 납품조립체 부품번호(LRU) */		
	private String make_part_no; 				/* 원제조사 부품번호 */
}
