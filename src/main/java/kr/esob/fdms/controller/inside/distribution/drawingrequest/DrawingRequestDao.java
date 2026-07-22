package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.drawing.approvalstatus.ApprovalStatusListVO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DrawingRequestDao extends AbstractDao {
	private String prefix = "sql.drawingRequest.";


	public List<DrawingRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<DrawingRequestTreeVO> selectTree(Object param){
		return list(prefix + "selectTree", param);
	}

	public List<DrawingRequestVO> selectPopupListInside(Object param){
		return list(prefix + "selectPopupListInside", param);
	}

	public List<ApprovalStatusListVO> selectPopupListOutside(Object param){
		return list("sql.ApprovalStatus.selectPopupListOutside", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

// 2023.07.10 기범 ( 등록 )
	public void insertDrawingRegisterInfo(DrawingRegisterPopupParam param) {
		insert(prefix + "insertDrawingRegisterInfo", param);
	}

	public void insertDrawingRevisionUpdate(DrawingRegisterPopupParam param) {
		insert(prefix + "insertDrawingRevisionUpdate", param);
	}

//	2023.07.21 기범(중복파일은 못올라감)
	@Autowired
	private SqlSession sqlSession;
	public String getDrawingRegisterByOrgFileNm(String objectId) {
		return sqlSession.selectOne(prefix + "getDrawingRegisterByOrgFileNm", objectId);
	}

    public DrawingRequestVO getPrevRevisionData(Object param) {
        return (DrawingRequestVO) obj(prefix + "selectPrevRivisionData", param);
    }

	public int deleteDrawing(Map<String, String> param) {
        return update(prefix + "updateDrawing", param);
    }

	public Map<String, Object> selectDrawingApprovalInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectDrawingApprovalInfo", objectId);
	}

	public int approveDrawing(Map<String, Object> param) {
		return update(prefix + "approveDrawing", param);
	}

	public int updateApprovedUsers(Map<String, Object> param) {
		return update(prefix + "updateApprovedUsers", param);
	}

	public void insertApprovalDetail(Map<String, Object> param) {
		insert(prefix + "insertApprovalDetail", param);
	}

	public int updateApprovalDetailApproved(Map<String, Object> param) {
		return update(prefix + "updateApprovalDetailApproved", param);
	}

	public List<Map<String, Object>> selectApprovalDetails(Map<String, Object> param) {
		return list(prefix + "selectApprovalDetails", param);
	}

	public List<Map<String, Object>> selectApprovalComments(String objectId) {
		return list(prefix + "selectApprovalComments", objectId);
	}

	public int upsertApprovalComment(Map<String, Object> param) {
		return update(prefix + "upsertApprovalComment", param);
	}

	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return list(prefix + "selectMainFileInfo", objectId);
	}

	public List<Map<String, Object>> selectSubFileInfo(String objectId) {
		return list(prefix + "selectSubFileInfo", objectId);
	}

	public Map<String, Object> selectDrawingFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) obj(prefix + "selectDrawingFileDownloadInfo", param);
	}

	public void insertDrawingSubFile(kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingSubFileParam param) {
		insert(prefix + "insertDrawingSubFile", param);
	}

	public int updateMainFileAfterCoverMerge(Map<String, Object> param) {
		return update(prefix + "updateMainFileAfterCoverMerge", param);
	}

	public int updateMainFileProcessingFail(Map<String, Object> param) {
		return update(prefix + "updateMainFileProcessingFail", param);
	}

	public String selectLatestRevisionNoByDrawingNo(String drawingNo) {
		return (String) obj(prefix + "selectLatestRevisionNoByDrawingNo", drawingNo);
	}

	public int hidePreviousRevisions(Map<String, String> param) {
		return update(prefix + "hidePreviousRevisions", param);
	}

	public List<Map<String, Object>> selectUserPositionByNames(Map<String, Object> param) {
		return list(prefix + "selectUserPositionByNames", param);
	}
}
