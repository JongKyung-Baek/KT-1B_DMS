package kr.esob.fdms.controller.inside.distribution.production;

import java.util.List;
import java.util.Map;

import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.controller.inside.distribution.swrequest.SwRegisterPopupParam;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ProductionRequestDao extends AbstractDao {
	private String prefix = "sql.DistributionProductionRequest.";


	public List<ProductionRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<ProductionRequestTreeVO> selectTree(Object param){
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
	public void insertProductionRegisterInfo(ProductionRegisterPopupParam param) {
		insert(prefix + "insertProductionRegisterInfo", param);
	}

	public void insertProductionRegisterInfoFile(ProductionRegisterPopupParam param){
		insert(prefix +"insertProductionRegisterInfoFile", param);
	}

	public void insertProductionSubFile(ProductionSubFileParam param) {
		insert(prefix + "insertProductionSubFile", param);
	}

	//	2023.07.21 기범(중복파일은 못올라감)
	@Autowired
	private SqlSession sqlSession;
	public String getProductionRegisterByOrgFileNm(String objectId) {
		return sqlSession.selectOne(prefix + "getProductionRegisterByOrgFileNm", objectId);
	}

    public int deletePrd(String objectId) {
        return update(prefix + "updatePrd", objectId);
	}

	public Map<String, Object> selectProductionApprovalInfo(String objectId) {
		return (Map<String, Object>) obj(prefix + "selectProductionApprovalInfo", objectId);
	}

	public int approveProduction(Map<String, Object> param) {
		return update(prefix + "approveProduction", param);
	}

	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return list(prefix + "selectMainFileInfo", objectId);
	}

	public List<Map<String, Object>> selectSubFileInfo(String objectId) {
		return list(prefix + "selectSubFileInfo", objectId);
	}

	public Map<String, Object> selectProductionFileDownloadInfo(Map<String, Object> param) {
		return (Map<String, Object>) obj(prefix + "selectProductionFileDownloadInfo", param);
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

	public Integer selectNextProductionRegisterNo(Map<String, Object> param) {
		return (Integer) obj(prefix + "selectNextProductionRegisterNo", param);
	}
}

