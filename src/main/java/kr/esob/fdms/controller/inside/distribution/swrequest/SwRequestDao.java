package kr.esob.fdms.controller.inside.distribution.swrequest;

import java.util.List;
import java.util.Map;

import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.controller.inside.distribution.docrequest.DocRegisterPopupParam;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class SwRequestDao extends AbstractDao {
	private String prefix = "sql.swRequest.";


	public List<SwRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<SwRequestTreeVO> selectTree(Object param){
		return list(prefix + "selectTree", param);
	}

	public List<ComboInfoVO> selectLevelOptions(Object param) {
		return list(prefix + "selectLevelOptions", param);
	}

	public List<ComboInfoVO> selectLevelParentOptions(Object param) {
		return list(prefix + "selectLevelParentOptions", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

//	public void updateList(Object param) {
//		update(prefix + "updateList", param);
//	}


	// 2023.07.24 기범 ( 등록 )
	public void insertSwRegisterInfo(SwRegisterPopupParam param) {
		insert(prefix + "insertSwRegisterInfo", param);
	}

	public void insertSwRegisterInfoFile(SwRegisterPopupParam param){
		insert(prefix +"insertSwRegisterInfoFile", param);
	}

	public void insertSwSubFile(SwSubFileParam param) {
		insert(prefix + "insertSwSubFile", param);
	}

	//	2023.07.21 기범(중복파일은 못올라감)
	@Autowired
	private SqlSession sqlSession;
	public String getSwRegisterByOrgFileNm(String objectId) {
		return sqlSession.selectOne(prefix + "getSwRegisterByOrgFileNm", objectId);
	}

    public int deleteSW(String objectId) {
        return update(prefix + "updateSW", objectId);
    }

	public Map<String, Object> selectSwApprovalInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectSwApprovalInfo", objectId);
	}

	public int approveSW(Map<String, Object> param) {
		return update(prefix + "approveSW", param);
	}

	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return list(prefix + "selectMainFileInfo", objectId);
	}

	public List<Map<String, Object>> selectSubFileInfo(String objectId) {
		return list(prefix + "selectSubFileInfo", objectId);
	}

	public Map<String, Object> selectSwFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) obj(prefix + "selectSwFileDownloadInfo", param);
	}

	public List<Map<String, Object>> selectApprovalComments(String objectId) {
		return list(prefix + "selectApprovalComments", objectId);
	}

	public int upsertApprovalComment(Map<String, Object> param) {
		return update(prefix + "upsertApprovalComment", param);
	}

	public List<Map<String, Object>> selectUserPositionByNames(Map<String, Object> param) {
		return list(prefix + "selectUserPositionByNames", param);
	}

	public int updateMainFileAfterCoverMerge(Map<String, Object> param) {
		return update(prefix + "updateMainFileAfterCoverMerge", param);
	}

	public int updateMainFileProcessingFail(Map<String, Object> param) {
		return update(prefix + "updateMainFileProcessingFail", param);
	}

	public Integer selectNextSwRegisterNo(Map<String, Object> param) {
		return (Integer) obj(prefix + "selectNextSwRegisterNo", param);
	}
}
