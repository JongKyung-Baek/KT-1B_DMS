package kr.esob.fdms.controller.outside.cr.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;

@Repository
public class RequestDao extends AbstractDao{
	private String prefix = "sql.OutsideCrRequest.";

	public List<DrawingInfoVO> getDrawingInfo(DrawingInfoVO drawingInfoVo) {
		return list(prefix + "getDrawingInfo", drawingInfoVo);
	}

	public void insertCrInfo(CrRequestParam param) {
		insert(prefix + "insertCrInfo", param);
	}

	public void insertCrFile(CrRequestParam param) {
		insert(prefix + "insertCrFile", param);
	}

	public void insertRequestFile(CrRequestParam param) {
		insert(prefix + "insertRequestFile", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public String selectCrNo() {
		return (String) obj(prefix + "selectCrNo", "");
	}

	public RequestStatusPopupVO getCrRequestInfo(CrRequestParam param) {
		return (RequestStatusPopupVO) obj(prefix + "getCrRequestInfo", param);
	}

	public void updateRequest(OutsideCrParam param) {
		update(prefix + "updateRequest", param);
	}

	public void updateRequestDetail(OutsideCrParam param) {
		update(prefix + "updateRequestDetail", param);
	}

	/**
	 * 외부업체팀장 승인/반려
	 * @param param
	 */
	public void updateCr(OutsideCrParam param) {
		update(prefix + "updateCr", param);
	}
}
