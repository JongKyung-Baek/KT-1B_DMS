package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import kr.esob.fdms.commonlogic.tree.TreeVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawingRequestTreeVO extends TreeVO {
	private String filterType;
	private String filterValue;
	private String drawingNoPrefix;
	private String distributeTypeCd;
}
