package kr.esob.fdms.controller.outside.sw.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;

@Repository
public class RequestDao extends AbstractDao {
	private String prefix = "sql.OuterSwRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<SwInfoVO> selectInspectionInfo(SwInfoVO swInfoVO) {
		return list(prefix + "selectInspectionInfo", swInfoVO);
	}

	public List<SwInfoVO> selectRequestStatusList(RequestParam param){
		return list(prefix + "selectRequestStatusList", param);
	}
	
	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}

}