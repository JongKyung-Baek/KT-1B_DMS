package kr.esob.fdms.controller.inside.production.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductStatusVO {
	private String objectNo;
	private String deptCd;
	private int currentCount;
	private String lastRequestNo;
	private String objectType;
	private String userCd;
	private String lastDeployRevNo;

}
