package kr.esob.fdms.controller.inside.production.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.inside.production.acceptance.AcceptancePopupParam;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductStatusVO;

@Repository
public class ApprovalDao extends AbstractDao {
	private String prefix = "sql.ProductionApproval.";

	@SuppressWarnings("unchecked")
	public List<ApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("rawtypes")
	public List selectRequestUserList(ApprovalPopupParam param) {
		return list(prefix + "selectRequestUserList", param);
	}

	@SuppressWarnings("rawtypes")
	public List selectReplaceRequestList(ApprovalPopupParam param) {
		return list(prefix + "selectReplaceRequestList", param);
	}

	@SuppressWarnings("rawtypes")
	public List selectObjectList(ApprovalPopupParam param) {
		return list(prefix + "selectObjectList", param);
	}

	public int selectRequestUserListCount(ApprovalPopupParam param) {
		return (Integer) obj(prefix + "selectRequestUserListCount", param);
	}

	public int selectObjectListCount(ApprovalPopupParam param) {
		return (Integer) obj(prefix + "selectObjectListCount", param);
	}

	public CommonApprovalParam getCurrentApprovalInfo(String requestNo) {
		return (CommonApprovalParam) obj(prefix + "getCurrentApprovalInfo", requestNo);
	}

	public void updateRequestInfo(ApprovalPopupParam param) {
		update(prefix + "updateRequestInfo", param);
	}

	public void updateRequestDetail(ApprovalPopupParam param) {
		update(prefix + "updateRequestDetail", param);
	}

	@SuppressWarnings("unchecked")
	public List<DeployInfoVO> selectDeployInfoList(AcceptancePopupParam param){
		return list(prefix + "selectDeployInfoList", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<DeployInfoVO> selectDeployInfoUserList(AcceptancePopupParam param){
		return list(prefix + "selectDeployInfoUserList", param);
	}

	public int selectProductStatusCount(DeployInfoVO param) {
		return (Integer)obj(prefix + "selectProductStatusCount", param);
	}

	public void updateProductStatus(AcceptancePopupParam param) {
		update(prefix + "updateProductStatus", param);
	}

	public void insertProductStatus(DeployInfoVO param) {
		insert(prefix + "insertProductStatus", param);
	}

	public ProductStatusVO selectProductionStatus(DeployInfoVO param) {
		return (ProductStatusVO) obj(prefix + "selectProductionStatus", param);
	}

	public void updateProductionStatus(ProductStatusVO param) {
		update(prefix + "updateProductionStatus", param);
	}

	public void insertProductionStatus(ProductStatusVO param) {
		insert(prefix + "insertProductionStatus", param);
	}

	public printApprovalVO getPrintRequestInfo(ApprovalPopupParam param) {
		return (printApprovalVO) obj(prefix + "getPrintRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<printApprovalVO> selectPrintApprovalrList(ApprovalPopupParam param) {
		return list(prefix + "selectPrintApprovalrList", param);
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param) {
		return (ApprovalPopupVO) obj(prefix + "getRequestInfo", param);
	}

	public void insertApprovalFile(ApprovalPopupParam param) {
		insert(prefix + "insertApprovalFile", param);

	}
	
	public void updateDeployInfoReject(ApprovalPopupParam param) {
		update(prefix + "updateDeployInfoReject", param);
	}
	
	public void updateDeployInfo(DeployInfoVO param) {
		update(prefix + "updateDeployInfo", param);
	}
}
