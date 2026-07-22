package kr.esob.fdms.controller.inside.system.menu;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestParam extends CommonParam {
	private String id;
	private String text;
	private String parent;
	private String useYn;
	private int sortSeq;
	private String roleCd;
	private String groupCd;
	private String saveFlag;
	private String authSite;
	private List<RequestParam> list;
}
