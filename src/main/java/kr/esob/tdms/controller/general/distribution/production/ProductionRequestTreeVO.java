package kr.esob.tdms.controller.general.distribution.production;

import kr.esob.tdms.commonlogic.tree.TreeVO;
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
