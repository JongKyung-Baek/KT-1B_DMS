package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsListParam extends CommonParam {
	private String business_area_cd;
	private String vendor_nm;
	private String vendor_usernm;
	private String management_no;
	private String insertStartDt;
	private String insertEndDt;
}
