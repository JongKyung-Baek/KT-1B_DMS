package kr.esob.fdms.controller.outside.commondestroyrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyRequestParam extends CommonParam{
	private String destroyType;
	private String destroyTypeNm;
	private String filePk;
	private String requestType;

	private String requestNo;
	private String objectId;
	private String fileNo;

	private String businessAreaCd;
	private String approvalUser;

//	List<DestroyRequestParam> list;

}
