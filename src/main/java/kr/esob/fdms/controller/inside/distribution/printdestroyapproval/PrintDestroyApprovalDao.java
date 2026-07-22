package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PrintDestroyApprovalDao extends AbstractDao {
	private String prefix = "sql.PrintDestroyApproval.";


	@SuppressWarnings("unchecked")
	public List<PrintDestroyApprovalListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("rawtypes")
	public List selectPopupList(PrintDestroyApprovalListParam param) {
		return list(prefix + "selectPopupList", param);
	}

	public List<PrintDestroyApprovalListParam> selectSearchInfo(PrintDestroyApprovalListParam param) {
		return list(prefix + "selectSearchInfo", param);
	}

	public void updatePrintDestroyRequestInfo(PrintDestroyApprovalPopupParam param) {
		update(prefix + "updatePrintDestroyRequestInfo", param);

	}

	public void updatePrintDestroyRequestDetail(PrintDestroyApprovalPopupParam param) {
		update(prefix + "updatePrintDestroyRequestDetail", param);
	}

	public PrintDestroyApprovalPopupParam getDestroyRequestInfo(PrintDestroyApprovalPopupParam param) {
		return (PrintDestroyApprovalPopupParam) obj(prefix + "getDestroyRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<PrintDestroyItemListVO> selectDestroyItemList(PrintDestroyApprovalPopupParam param) {
		return list(prefix + "selectDestroyItemList", param);
	}

	public void updateRequestMapping(PrintDestroyApprovalPopupParam param) {
		update(prefix + "updateRequestMapping", param);
	}

	public PrintDestroyItemListVO getDestroyRequest(String destroyRequestNo) {
		return (PrintDestroyItemListVO) obj(prefix + "getDestroyRequest", destroyRequestNo);
	}

	public int selectPopupListCount(PrintDestroyApprovalListParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

}
