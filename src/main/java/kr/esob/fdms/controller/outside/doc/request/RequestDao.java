package kr.esob.fdms.controller.outside.doc.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Repository
public class RequestDao extends AbstractDao {
	private String prefix = "sql.OuterDocRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<ObjectInfoVO> selectInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectInspectionInfo", param);
	}

	public List<DocInfoVO> selectRequestStatusList(RequestParam param){
		return list(prefix + "selectRequestStatusList", param);
	}

	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}

}