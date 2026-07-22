package kr.esob.fdms.controller.main;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class MainDao extends AbstractDao {
	private String prefix = "sql.Main.";

	/**
	 * 내가 접수 해야 할 수(배포요청접수)
	 * @param param
	 * @return
	 */
	public Integer selectHaveToAcceptDistributionCount(Object param){
		return (Integer) obj(prefix + "selectHaveToAcceptDistributionCount", param);
	}

	/**
	 * 내가 접수 해야 할 수(생산기술자료)
	 * @param param
	 * @return
	 */
	public Integer selectHaveToAcceptProductionCount(Object param){
		return (Integer) obj(prefix + "selectHaveToAcceptProductionCount", param);
	}

	/**
	 * 로그인 한 사용자가 방산 결재자인지. 맞으면 1, 아니면 0
	 * @param param
	 * @return
	 */
	public Integer selectProtectUserCount(Object param){
		return (Integer) obj(prefix + "selectProtectUserCount", param);
	}

	/**
	 * 팀장 - CR 내가 승인해야 할 요청 수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalCrCount(Object param){
		return (Integer) obj(prefix + "selectHaveToApprovalCrCount", param);
	}
	
	/**
	 * cr - 내가 접수해야 할 건수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToAcceptCrCount(Object param){
		return (Integer) obj(prefix + "selectHaveToAcceptCrCount", param);
	}

	/**
	 * 내가 거절 한 수 - cr
	 * @param param
	 * @return
	 */
	public Integer selectRejectCrCount(Object param){
		return (Integer) obj(prefix + "selectRejectCrCount", param);
	}

	/**
	 * 승인해야 할 수 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectHaveToPrintDisposalCount(Object param){
		return (Integer) obj(prefix + "selectHaveToPrintDisposalCount", param);
	}

	/**
	 * 반려 한 수 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectPrintDisposalCount(Object param){
		return (Integer) obj(prefix + "selectRejectPrintDisposalCount", param);
	}

	/**
	 * 승인해야 할 수 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectHaveToProductionDisposalCount(Object param){
		return (Integer) obj(prefix + "selectHaveToProductionDisposalCount", param);
	}

	/**
	 * 반려 한 수 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectProductionDisposalCount(Object param){
		return (Integer) obj(prefix + "selectRejectProductionDisposalCount", param);
	}

	/**
	 * 반려된 수 - 생산기술 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectedPrintDisposalCount(Object param){
		return (Integer) obj(prefix + "selectRejectedPrintDisposalCount", param);
	}

	/**
	 * 반려된 수 - 미등록자료
	 * @param param
	 * @return
	 */
	public Integer selectRejectedUnregCount(Object param){
		return (Integer) obj(prefix + "selectRejectedUnregCount", param);
	}

	/**
	 * 반려된 수 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectedProductionDisposalCount(Object param){
		return (Integer) obj(prefix + "selectRejectedProductionDisposalCount", param);
	}

	/**
	 * 반려된 수 - 출력
	 * @param param
	 * @return
	 */
	public Integer selectPrintRejectedCount(Object param){
		return (Integer) obj(prefix + "selectPrintRejectedCount", param);
	}

	/**
	 * 승인된 수 - 배포
	 * @param param
	 * @return
	 */
	public Integer selectApprovedDistributionCount(Object param){
		return (Integer) obj(prefix + "selectApprovedDistributionCount", param);
	}

	/**
	 * 승인된 수 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectApprovedProductionDisposalCount(Object param){
		return (Integer) obj(prefix + "selectApprovedProductionDisposalCount", param);
	}

	/**
	 * 승인된 수 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectApprovedPrintDisposalCount(Object param){
		return (Integer) obj(prefix + "selectApprovedPrintDisposalCount", param);
	}

	/**
	 * 요청한 수 - 출력 폐기
	 * @param param
	 * @return
	 */
	public Integer selectRequestPrintDisposalCount(Object param){
		return (Integer) obj(prefix + "selectRequestPrintDisposalCount", param);
	}

	/**
	 * 특정 CR 상태 count
	 * @param param
	 * @return
	 */
	public Integer selectCrCount(Object param){
		return (Integer) obj(prefix + "selectCrCount", param);
	}

	/**
	 * REQUEST 테이블에서 필요한 데이터 수를 가져온다.
	 * @param param
	 * @return
	 */
	public Integer selectDocsRequestCount(Object param){
		return (Integer) obj(prefix + "selectDocsRequestCount", param);
	}

	/**
	 * 내가 거절 한 수
	 * @param param
	 * @return
	 */
	public Integer selectRejectCount(Object param){
		return (Integer) obj(prefix + "selectRejectCount", param);
	}

	/**
	 * 팀장 - 내가 승인해야 할 수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalCount(Object param){
		return (Integer) obj(prefix + "selectHaveToApprovalCount", param);
	}

	/**
	 * 유효기간 임박
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ValidEndListVO> selectValidEnd(Object param){
		return list(prefix + "selectValidEnd", param);
	}

	@SuppressWarnings("unchecked")
	public List<ValidEndListVO> selectPrintDisposalValidEnd(Object param){
		return list(prefix + "selectPrintDisposalValidEnd", param);
	}

	@SuppressWarnings("unchecked")
	public List<ValidEndListVO> selectPendingApprovalList(Object param){
		return list(prefix + "selectPendingApprovalList", param);
	}

	@SuppressWarnings("unchecked")
	public List<ValidEndListVO> selectPendingReviewList(Object param){
		return list(prefix + "selectPendingReviewList", param);
	}

	/**
	 * 세션 시간 조회
	 */
	public Integer selectSessionTime(){
		return (Integer) obj(prefix + "selectSessionTime");
	}

	/**
	 * 세션 시간 업데이트
	 */
	public int updateSessionTime(int sessionTime) {
		return update(prefix + "updateSessionTime", sessionTime);
	}

	public String selectRoleGroupByUserCd(UserVO sessionUser){
		return (String) obj(prefix + "selectRoleGroupByUserCd", sessionUser.getUserCd());
	}


}
