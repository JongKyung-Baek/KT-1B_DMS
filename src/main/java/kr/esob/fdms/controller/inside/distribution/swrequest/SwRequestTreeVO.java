package kr.esob.fdms.controller.inside.distribution.swrequest;

import kr.esob.fdms.commonlogic.tree.TreeVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwRequestTreeVO extends TreeVO {
	private String filterType;
	private String filterValue;
	private String swNoPrefix;
	private String swTreeCd;
	private String distributeTypeCd;
}
