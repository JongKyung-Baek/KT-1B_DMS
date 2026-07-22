package kr.esob.fdms.controller.inside.distribution.docrequest;

import kr.esob.fdms.commonlogic.tree.TreeVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocRequestTreeVO extends TreeVO {
	private String filterType;
	private String filterValue;
	private String documentNoPrefix;
	private String distributeTypeCd;
}
