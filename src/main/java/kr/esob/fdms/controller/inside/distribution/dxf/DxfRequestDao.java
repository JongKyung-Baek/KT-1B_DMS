package kr.esob.fdms.controller.inside.distribution.dxf;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DxfRequestDao extends AbstractDao {
	private String prefix = "sql.DxfRequest.";


	public List<DxfRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<DxfRequestTreeVO> selectTree(Object param){
		return list(prefix + "selectTree", param);
	}

	public List<ComboInfoVO> selectLevelOptions(Object param) {
		return list(prefix + "selectLevelOptions", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

//	public void updateList(Object param) {
//		update(prefix + "updateList", param);
//	}


	// 2023.07.24 기범 ( 등록 )
	public void insertDxfRegisterInfo(DxfRegisterPopupParam param) {
		insert(prefix + "insertDxfRegisterInfo", param);
	}

	public void insertDxfRegisterInfoFile(DxfRegisterPopupParam param){
		insert(prefix +"insertDxfRegisterInfoFile", param);
	}

	public void insertDxfSubFile(DxfSubFileParam param) {
		insert(prefix + "insertDxfSubFile", param);
	}

	//	2023.07.21 기범(중복파일은 못올라감)
	@Autowired
	private SqlSession sqlSession;
	public String getDxfRegisterByOrgFileNm(String objectId) {
		return sqlSession.selectOne(prefix + "getDxfRegisterByOrgFileNm", objectId);
	}

    public int deleteDXF(String objectId) {
        return update(prefix + "updateDXF", objectId);
    }

	public Map<String, Object> selectDxfApprovalInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectDxfApprovalInfo", objectId);
	}

	public int approveDxf(Map<String, Object> param) {
		return update(prefix + "approveDxf", param);
	}

	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return list(prefix + "selectMainFileInfo", objectId);
	}

	public List<Map<String, Object>> selectSubFileInfo(String objectId) {
		return list(prefix + "selectSubFileInfo", objectId);
	}

	public Map<String, Object> selectDxfFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) obj(prefix + "selectDxfFileDownloadInfo", param);
	}

	public List<Map<String, Object>> selectApprovalComments(String objectId) {
		return list(prefix + "selectApprovalComments", objectId);
	}

	public int upsertApprovalComment(Map<String, Object> param) {
		return update(prefix + "upsertApprovalComment", param);
	}


	public int updateMainFileAfterCoverMerge(Map<String, Object> param) {
		return update(prefix + "updateMainFileAfterCoverMerge", param);
	}

	public int updateMainFileProcessingFail(Map<String, Object> param) {
		return update(prefix + "updateMainFileProcessingFail", param);
	}

	public Integer selectNextDxfRegisterNo(Map<String, Object> param) {
		return (Integer) obj(prefix + "selectNextDxfRegisterNo", param);
	}

	public List<Map<String, Object>> selectUserPositionByNames(Map<String, Object> param) {
		return list(prefix + "selectUserPositionByNames", param);
	}
}
