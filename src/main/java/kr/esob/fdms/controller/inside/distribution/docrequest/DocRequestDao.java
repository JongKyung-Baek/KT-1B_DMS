package kr.esob.fdms.controller.inside.distribution.docrequest;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class DocRequestDao extends AbstractDao {
	private String prefix = "sql.docRequest.";


	public List<DocRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<DocRequestTreeVO> selectTree(Object param){
		return list(prefix + "selectTree", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

//	public void updateList(Object param) {
//		update(prefix + "updateList", param);
//	}


	// 2023.07.24 기범 ( 등록 )
	public void insertDocRegisterInfo(DocRegisterPopupParam param) {
		insert(prefix + "insertDocRegisterInfo", param);
	}

	public void insertDocReigsterInfoFile(DocRegisterPopupParam param){
		insert(prefix +"insertDocReigsterInfoFile", param);
	}

	@Autowired
	private SqlSession sqlSession;
	public String getDocRegisterByOrgFileNm(String objectId) {
		return sqlSession.selectOne(prefix + "getDocRegisterByOrgFileNm", objectId);
	}

    public int deleteDoc(String objectId) {
		return update(prefix + "updateDoc", objectId);
    }

	public Integer selectNextDocRegisterNo(Map<String, Object> param) {
		return (Integer) obj(prefix + "selectNextDocRegisterNo", param);
	}

	public Map<String, Object> selectDocWithdrawInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectDocWithdrawInfo", objectId);
	}

	public Map<String, Object> selectDocApprovalInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectDocApprovalInfo", objectId);
	}

	public int approveDocument(Map<String, Object> param) {
		return update(prefix + "approveDocument", param);
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

	public Map<String, Object> selectDocFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) obj(prefix + "selectDocFileDownloadInfo", param);
	}

	public void insertDocSubFile(DocSubFileParam param) {
		insert(prefix + "insertDocSubFile", param);
	}

	public int updateMainFileAfterCoverMerge(Map<String, Object> param) {
		return update(prefix + "updateMainFileAfterCoverMerge", param);
	}

	public int updateMainFileProcessingFail(Map<String, Object> param) {
		return update(prefix + "updateMainFileProcessingFail", param);
	}

	public List<Map<String, Object>> selectUserPositionByNames(Map<String, Object> param) {
		return list(prefix + "selectUserPositionByNames", param);
	}

}
