package kr.esob.tdms.controller.general.distribution.drawingrequest;

import kr.esob.tdms.commonlogic.tree.TreeVO;
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
