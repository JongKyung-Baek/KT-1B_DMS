package kr.esob.fdms.controller.main;

import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class MainService {

	@Inject
	MainDao dao;

	/**
	 * 내가 접수 해야 할 수 - 배포요청접수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToAcceptDistributionCount(MainParam param) {
		return dao.selectHaveToAcceptDistributionCount(param);
	}

	/**
	 * 내가 접수 해야 할 수 - 생산기술자료
	 * @param param
	 * @return
	 */
	public Integer selectHaveToAcceptProductionCount(MainParam param) {
		return dao.selectHaveToAcceptProductionCount(param);
	}

	/**
	 * 로그인 한 사용자가 방산결재권한이 있는지 여부
	 * @param param
	 * @return
	 */
	public boolean isProtectApprovalUser(Object param){
		return dao.selectProtectUserCount(param) > 0 ? true : false;
	}

	/**
	 * 팀장 - 내가 승인해야 할 배포요청 수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalDistributionCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);

		return dao.selectHaveToApprovalCount(param);
	}

	/**
	 * 팀장 - 내가 승인해야 할 생산기술 수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalProductionCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);

		return dao.selectHaveToApprovalCount(param);
	}

	/**
	 * 내가 승인해야 할 cr 수
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalCrCount(Object param){
		return dao.selectHaveToApprovalCrCount(param);
	}
	
	public Integer selectHaveToAcceptCrCount(Object param){
		return dao.selectHaveToAcceptCrCount(param);
	}

	/**
	 * 팀장 - 내가 승인해야 할 미등록 자료
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalUnregCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_NOREG);

		return dao.selectHaveToApprovalCount(param);
	}

	/**
	 * 내가 출력승인해야 할 목록
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);

		return dao.selectHaveToApprovalCount(param);
	}

	/**
	 * 팀장 - 내가 출력승인해야 할 목록 - 생산기술자료
	 * @param param
	 * @return
	 */
	public Integer selectHaveToApprovalProductionPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT_PRINT);

		return dao.selectHaveToApprovalCount(param);
	}

	/**
	 * 내가 거절한 수 - 자료배포
	 * @param param
	 * @return
	 */
	public Integer selectDistributionRejectCount(MainParam param){
		param.setStatusCd(Constant.REJECT);
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);

		return dao.selectRejectCount(param);
	}

	/**
	 * 내가 거절한 수 - 출력요청
	 * @param param
	 * @return
	 */
	public Integer selectPrintRejectCount(MainParam param){
		param.setStatusCd(Constant.REJECT);
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);

		return dao.selectRejectCount(param);
	}

	/**
	 * 내가 거절한 수 -cr
	 * @param param
	 * @return
	 */
	public Integer selectRejectCrCount(Object param){
		return dao.selectRejectCrCount(param);
	}

	/**
	 * 내가 거절한 수 - 미등록자료
	 * @param param
	 * @return
	 */
	public Integer selectRejectUnregCount(MainParam param){
		param.setStatusCd(Constant.REJECT);
		param.setRequestType(Constant.REQUEST_TYPE_NOREG);

		return dao.selectRejectCount(param);
	}

	/**
	 * 내가 거절한 수- 생산기술자료
	 * @param param
	 * @return
	 */
	public Integer selectRejectProductionCount(MainParam param){
		param.setStatusCd(Constant.REJECT);
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);

		return dao.selectRejectCount(param);
	}

	/**
	 * 내가 거절한 수 - 생산기술자료출력
	 * @param param
	 * @return
	 */
	public Integer selectRejectProductionPrintCount(MainParam param){
		param.setStatusCd(Constant.REJECT);
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT_PRINT);

		return dao.selectRejectCount(param);
	}

	/**
	 * 승인해야 할 목록 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectHaveToPrintDisposalCount(Object param){
		return dao.selectHaveToPrintDisposalCount(param);
	}

	/**
	 * 출력폐기 한 수
	 * @param param
	 * @return
	 */
	public Integer selectRejectPrintDisposalCount(Object param){
		return dao.selectRejectedPrintDisposalCount(param);
	}

	/**
	 * 승인해야 할 목록 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectHaveToProductionDisposalCount(Object param){
		return dao.selectHaveToProductionDisposalCount(param);
	}

	/**
	 * 반려 한 목록 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectProductionDisposalCount(Object param){
		return dao.selectRejectProductionDisposalCount(param);
	}

	/**
	 * 반려 된 수 - 배포요청
	 * @param param
	 * @return
	 */
	public Integer selectRejectedDistributionCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.REJECT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 반려된 수 - 생산기술 출력
	 * @param param
	 * @return
	 */
	public Integer selectRejectedProductPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT_PRINT);
		param.setStatusCd(Constant.REJECT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 반려된 수 - 생산기술 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectedPrintDisposalCount(Object param){
		return dao.selectRejectedPrintDisposalCount(param);
	}

	/**
	 * 반려된 수 - 미등록자료
	 * @param param
	 * @return
	 */
	public Integer selectRejectedUnregCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_NOREG);
		param.setStatusCd(Constant.REJECT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 반려된 수 - 생산기술자료 폐기
	 * @param param
	 * @return
	 */
	public Integer selectRejectedProductionDisposalCount(Object param){
		return dao.selectRejectedProductionDisposalCount(param);
	}

	/**
	 * 반려된 수 - 출력
	 * @param param
	 * @return
	 */
	public Integer selectPrintRejectedCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);
		param.setStatusCd(Constant.REJECT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 배포
	 * @param param
	 * @return
	 */
	public Integer selectApprovedDistributionCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.APPROVAL);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 미등록자료
	 * @param param
	 * @return
	 */
	public Integer selectApprovedUnregCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_NOREG);
		param.setStatusCd(Constant.ACCEPT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 생산기술
	 * @param param
	 * @return
	 */
	public Integer selectApprovedProductionCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);
		param.setStatusCd(Constant.ACCEPT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 생산기술 출력
	 * @param param
	 * @return
	 */
	public Integer selectApprovedProductionPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT_PRINT);
		param.setStatusCd(Constant.APPROVAL);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 생산기술 폐기
	 * @param param
	 * @return
	 */
	public Integer selectApprovedProductionDisposalCount(Object param){
		return dao.selectApprovedProductionDisposalCount(param);
	}

	/**
	 * 승인된 수 - 출력
	 * @param param
	 * @return
	 */
	public Integer selectApprovedPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);
		param.setStatusCd(Constant.ACCEPT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 승인된 수 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectApprovedPrintDisposalCount(Object param){
		return dao.selectApprovedPrintDisposalCount(param);
	}

	/**
	 * 배포요청한 수
	 * @param param
	 * @return
	 */
	public Integer selectRequestCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 요청 한 수 - 출력
	 * @param param
	 * @return
	 */
	public Integer selectRequestPrintCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 요청한 수 - 출력폐기
	 * @param param
	 * @return
	 */
	public Integer selectRequestPrintDisposalCount(Object param){
		return dao.selectRequestPrintDisposalCount(param);
	}

	/**
	 * 도면 - 접수된것
	 * @param param
	 * @return
	 */
	public Integer selectAcceptedDrawingCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.ACCEPT);
		param.setObjectType(Constant.OBJECT_TYPE_DRAWING);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 문서 - 접수된것
	 * @param param
	 * @return
	 */
	public Integer selectAcceptedDocCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.ACCEPT);
		param.setObjectType(Constant.OBJECT_TYPE_DOC);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * sw - 접수된것
	 * @param param
	 * @return
	 */
	public Integer selectAcceptedSwCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.ACCEPT);
		param.setObjectType(Constant.OBJECT_TYPE_SW);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * CR- 접수된것
	 * @param param
	 * @return
	 */
	public Integer selectAcceptedCrCount(MainParam param){
		param.setStatusCd(Constant.ACCEPT);

		return dao.selectCrCount(param);
	}

	/**
	 * 생산기술 - 접수된것
	 * @param param
	 * @return
	 */
	public Integer selectAcceptedProductCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);
		param.setStatusCd(Constant.ACCEPT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 도면 - 승인된것
	 * @param param
	 * @return
	 */
	public Integer selectApprovedDrawingCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.APPROVAL);
		param.setObjectType(Constant.OBJECT_TYPE_DRAWING);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 도면 - 반려된것
	 * @param param
	 * @return
	 */
	public Integer selectRejectedDrawingCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.REJECT);
		param.setObjectType(Constant.OBJECT_TYPE_DRAWING);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 문서 - 승인된것
	 * @param param
	 * @return
	 */
	public Integer selectApprovedDocCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.APPROVAL);
		param.setObjectType(Constant.OBJECT_TYPE_DOC);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 문서 - 반려된것
	 * @param param
	 * @return
	 */
	public Integer selectRejectedDocCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.REJECT);
		param.setObjectType(Constant.OBJECT_TYPE_DOC);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * SW - 승인된것
	 * @param param
	 * @return
	 */
	public Integer selectApprovedSwCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.APPROVAL);
		param.setObjectType(Constant.OBJECT_TYPE_SW);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * SW - 반려된것
	 * @param param
	 * @return
	 */
	public Integer selectRejectedSwCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);
		param.setStatusCd(Constant.REJECT);
		param.setObjectType(Constant.OBJECT_TYPE_SW);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * CR - 승인된것
	 * @param param
	 * @return
	 */
	public Integer selectApprovedCrCount(MainParam param){
		param.setStatusCd(Constant.APPROVAL);

		return dao.selectCrCount(param);
	}

	public Integer selectRejectedCrCount(MainParam param){
		param.setStatusCd(Constant.REJECT);

		return dao.selectCrCount(param);
	}

	/**
	 * 생산기술 - 승인된것
	 * @param param
	 * @return
	 */
	public Integer selectApprovedProductCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);
		param.setStatusCd(Constant.APPROVAL);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 생산기술 - 반려된것
	 * @param param
	 * @return
	 */
	public Integer selectRejectedProductCount(MainParam param){
		param.setRequestType(Constant.REQUEST_TYPE_PRODUCT);
		param.setStatusCd(Constant.REJECT);

		return dao.selectDocsRequestCount(param);
	}

	/**
	 * 출력기한 임박자료
	 * @param param
	 * @return
	 */
	public List<ValidEndListVO> selectPrintValidEnd(MainParam param) {
		param.setRequestType(Constant.REQUEST_TYPE_PRINT);

		return dao.selectValidEnd(param);
	}

	/**
	 * 폐기기한
	 * @param param
	 * @return
	 */
	public List<ValidEndListVO> selectDisposalValidEnd(MainParam param) {
		param.setRequestType(Constant.REQUEST_TYPE_DISTRIBUTION);

		return dao.selectValidEnd(param);
	}

	/**
	 * 출력물 폐기기한 임박
	 * @param param
	 * @return
	 */
	public List<ValidEndListVO> selectPrintDisposalValidEnd(MainParam param) {
		return dao.selectPrintDisposalValidEnd(param);
	}

	public List<ValidEndListVO> selectPendingApprovalList(MainParam param) {
		return dao.selectPendingApprovalList(param);
	}

	public List<ValidEndListVO> selectPendingReviewList(MainParam param) {
		return dao.selectPendingReviewList(param);
	}

	/**
	 * 세션 시간 조회
	 */
	public Integer selectSessionTime() {
		return dao.selectSessionTime();
	}

	/**
	 * 세션 시간 업데이트
	 */
	public int updateSessionTime(int sessionTime) {
		return dao.updateSessionTime(sessionTime);
	}

	public String selectRoleGroupByUserCd(UserVO userVo) {
		return dao.selectRoleGroupByUserCd(userVo);
	}
}
