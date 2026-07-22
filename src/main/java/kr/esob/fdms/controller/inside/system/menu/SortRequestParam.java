package kr.esob.fdms.controller.inside.system.menu;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortRequestParam extends CommonParam {
	private String id;
	private int sortSeq;
	private String parent;
	private String menuType;
	private String authSite;
	private String menuLevel;
	private String treeType;
	private List<SortRequestParam> list;
}
