package kr.esob.fdms.controller.outside.drawing.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Repository
public class RequestDao extends AbstractDao {
	private String prefix = "sql.OuterDrawingRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<DrawingInfoVO> selectInspectionInfo(DrawingInfoVO drawingInfoVO) {
		return list(prefix + "selectInspectionInfo", drawingInfoVO);
	}

	public List<DrawingInfoVO> selectRequestStatusList(RequestParam param){
		return list(prefix + "selectRequestStatusList", param);
	}

	public List<DrawingInfoVO> selectDrawingDetailList(DrawingInfoVO drawingInfoVO){
		return list(prefix + "selectDrawingDetailList", drawingInfoVO);
	}
	
	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}

}
