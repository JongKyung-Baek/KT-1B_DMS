package kr.esob.fdms.commonlogic.down;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class CommonDownParam extends CommonParam{
	private String objectId;
	private String fileNo;
	private String gridId;
	
	List<CommonDownParam> list;
}
