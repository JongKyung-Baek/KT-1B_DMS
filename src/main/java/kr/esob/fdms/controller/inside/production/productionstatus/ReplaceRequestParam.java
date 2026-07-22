package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplaceRequestParam extends CommonParam{
	private String objectNo;
	private String deptCd;
	private String objectType;

	private String param;

	List<ReplaceRequestParam> list;
}
