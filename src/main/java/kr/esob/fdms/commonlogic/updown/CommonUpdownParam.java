package kr.esob.fdms.commonlogic.updown;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class CommonUpdownParam extends CommonParam{
	private String requestNo;
	private String docSeq;
	private String dataNo;
	private String objectType;
	private String requestType;
	private String fileNo;
	private String fileSeq;
	
	private String userNm;
	private String gridId;
	private String distributeTypeCd;
	private String reqType;

	private String selectedDataJson;
	

	List<CommonUpdownParam> list;
}
