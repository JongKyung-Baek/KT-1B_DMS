package kr.esob.fdms.controller.inside.distribution.history;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
import kr.esob.fdms.controller.outside.commondestroyrequest.DestroyRequestParam;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.DistributionHistory.";

	@SuppressWarnings("unchecked")
	public List<HistoryListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List selectCompanyList(stopDealPopupParam param) {
		return list(prefix + "selectCompanyList", param);
	}

	public int selectCompanyListCount(stopDealPopupParam param) {
		return (Integer) obj(prefix + "selectCompanyListCount", param);
	}

	/**
	 * 업체 거래 중단. 업체 삭제 처리
	 * @param param
	 */
	public void deleteCompany(CompanyListVO param) {
		update(prefix + "deleteCompany", param);
	}

	/**
	 * 업체 거래 중단. 사용자 삭제 처리
	 * @param param
	 */
	public void deleteUser(CompanyListVO param) {
		update(prefix + "deleteUser", param);
	}

	public void destroyDistribution(stopDealPopupParam param) {
		update(prefix + "destroyDistribution", param);
	}

	public void deleteVendor(CompanyListVO param) {
		update(prefix + "deleteVendor", param);
	}

	public void insertDestroyInfo(DestroyPopupParam param) {
		insert(prefix + "insertDestroyInfo", param);
	}

	public void insertDestroyFile(DestroyPopupParam param) {
		insert(prefix + "insertDestroyFile", param);
	}

	public void updateDestroyRequest(DestroyPopupParam param) {
		update(prefix + "updateDestroyRequest", param);
	}

	public void updateDeistributeInfo(DestroyPopupParam param) {
		update(prefix + "updateDeistributeInfo", param);
	}

	public List<stopDealPopupParam> selectDestroyList(String companyCd) {
		return list(prefix + "selectDestroyList", companyCd);
	}

	public void insertDestroyRequest(stopDealPopupParam param) {
		insert(prefix + "insertDestroyRequest", param);
	}

	public void insertDestroyRequestDetail(stopDealPopupParam param) {
		insert(prefix + "insertDestroyRequestDetail", param);
	}

	public List getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public void insertDestroyRequestMapping(stopDealPopupParam param) {
		insert(prefix + "insertDestroyRequestMapping", param);

	}

	public void updateDestroyRequestDesc(DestroyPopupParam param) {
		update(prefix + "updateDestroyRequestDesc", param);
	}

	public void updateRequestFileDestroy(CompanyListVO param) {
		update(prefix + "updateRequestFileDestroy", param);
	}

	public void updateUnderDestroy(HistoryListVO param) {
		update(prefix + "updateUnderDestroy", param);
	}
}
