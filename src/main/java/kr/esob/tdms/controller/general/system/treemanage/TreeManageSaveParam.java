package kr.esob.tdms.controller.general.system.treemanage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TreeManageSaveParam {
	private String treeCd;
	private String functionCd;
	private String parentTreeCd;
	private String treeNm;
	private String manageType;
}
