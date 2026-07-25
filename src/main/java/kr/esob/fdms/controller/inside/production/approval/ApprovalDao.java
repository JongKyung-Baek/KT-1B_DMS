package kr.esob.fdms.controller.inside.production.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
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

	public ApprovalPopupParam getCurrentApprovalInfo(ApprovalPopupParam param) {
		return (ApprovalPopupParam) obj(prefix + "getCurrentApprovalInfo", param);
	}

	public int updateRequestInfo(ApprovalPopupParam param) {
		return update(prefix + "updateRequestInfo", param);
	}

	public int updateRequestDetail(ApprovalPopupParam param) {
		return update(prefix + "updateRequestDetail", param);
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

	public int updateProductionStatus(ProductStatusVO param) {
		return update(prefix + "updateProductionStatus", param);
	}

	public int insertProductionStatus(ProductStatusVO param) {
		return (Integer) insert(prefix + "insertProductionStatus", param);
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

	public int insertApprovalFile(ApprovalPopupParam param) {
		return (Integer) insert(prefix + "insertApprovalFile", param);
	}
	
	public int updateDeployInfoReject(ApprovalPopupParam param) {
		return update(prefix + "updateDeployInfoReject", param);
	}
	
	public int updateDeployInfo(DeployInfoVO param) {
		return update(prefix + "updateDeployInfo", param);
	}
}
