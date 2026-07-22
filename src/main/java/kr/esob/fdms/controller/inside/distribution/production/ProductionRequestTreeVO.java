package kr.esob.fdms.controller.inside.distribution.production;

import kr.esob.fdms.commonlogic.tree.TreeVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionRequestTreeVO extends TreeVO {
	private String filterType;
	private String filterValue;
	private String objectNoPrefix;
	private String distributeTypeCd;
}
