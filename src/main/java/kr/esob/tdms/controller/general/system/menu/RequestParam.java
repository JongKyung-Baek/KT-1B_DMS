package kr.esob.tdms.controller.general.system.menu;

import java.util.List;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
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
	private List<RequestParam> list;
}
