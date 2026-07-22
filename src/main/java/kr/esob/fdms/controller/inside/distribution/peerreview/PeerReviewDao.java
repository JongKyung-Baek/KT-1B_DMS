package kr.esob.fdms.controller.inside.distribution.peerreview;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PeerReviewDao extends AbstractDao {
	private String prefix = "sql.PeerReview.";

	@SuppressWarnings("unchecked")
	public List<PeerReviewListVO> selectList(Object param) {
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param) {
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public String selectNextPeerReviewNo() {
		return (String) obj(prefix + "selectNextPeerReviewNo");
	}

	public Integer selectNextCnSerial() {
		return (Integer) obj(prefix + "selectNextCnSerial");
	}

	public void insertPeerReview(PeerReviewSaveParam param) {
		insert(prefix + "insertPeerReview", param);
	}

	public int updateConvertedMainFile(Map<String, Object> param) {
		return update(prefix + "updateConvertedMainFile", param);
	}

	public int updateProcessingStatus(Map<String, Object> param) {
		return update(prefix + "updateProcessingStatus", param);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectPeerReviewApprovalInfo(String objectId) {
		return (Map<String, Object>) get(prefix + "selectPeerReviewApprovalInfo", objectId);
	}

	public int deletePeerReview(Map<String, String> param) {
		return update(prefix + "deletePeerReview", param);
	}

	public int approvePeerReview(Map<String, Object> param) {
		return update(prefix + "approvePeerReview", param);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return list(prefix + "selectMainFileInfo", objectId);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMainFileInfoByPeerReviewNo(String peerreviewNo) {
		return list(prefix + "selectMainFileInfoByPeerReviewNo", peerreviewNo);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectPeerReviewFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) get(prefix + "selectPeerReviewFileDownloadInfo", param);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectPeerReviewFileDownloadInfoByPeerReviewNo(String peerreviewNo) {
		return (Map<String, Object>) get(prefix + "selectPeerReviewFileDownloadInfoByPeerReviewNo", peerreviewNo);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectApprovalComments(String objectId) {
		return list(prefix + "selectApprovalComments", objectId);
	}

	public int upsertApprovalComment(Map<String, Object> param) {
		return update(prefix + "upsertApprovalComment", param);
	}

	public String selectObjectIdByPeerReviewNo(String peerreviewNo) {
		return (String) obj(prefix + "selectObjectIdByPeerReviewNo", peerreviewNo);
	}

	public String selectPeerReviewNoByObjectId(String objectId) {
		return (String) obj(prefix + "selectPeerReviewNoByObjectId", objectId);
	}
}
