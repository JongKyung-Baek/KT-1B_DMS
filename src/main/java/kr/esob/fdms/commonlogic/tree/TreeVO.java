package kr.esob.fdms.commonlogic.tree;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TreeVO {
	private String id;
	private String parent;
	private String text;
	private String level;
	private String type;
	private Integer sort;
	private String useYn;
	private String roleCd;
	private boolean isVisible = true;

}
