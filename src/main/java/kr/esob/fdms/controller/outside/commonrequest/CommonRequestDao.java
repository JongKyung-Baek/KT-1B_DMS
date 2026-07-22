package kr.esob.fdms.controller.outside.commonrequest;

import java.util.List;
import java.util.Map;

import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestVO;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;

@Repository
public class CommonRequestDao extends AbstractDao {
	private String prefix = "sql.CommonRequestOutside.";

	public void insertDocsRequest(RequestParam param) {
		insert(prefix + "insertDocsRequest", param);
	}

	public void insertDocsRequestMapping(RequestParam param) {
		insert(prefix + "insertDocsRequestMapping", param);
	}

	public void insertDocsRequestDetail(RequestParam param) {
		insert(prefix + "insertDocsRequestDetail", param);
	}

	public void insertDocsRequestDeploy(RequestParam param) {
		insert(prefix + "insertDocsRequestDeploy", param);
	}

	public UserVO selectUserInfo(UserVO userVo) {
		return (UserVO) obj(prefix + "selectUserInfo", userVo);
	}

	public UserVO selectUserEmail(UserVO userVo) {
		return (UserVO) obj(prefix + "selectUserEmail", userVo);
	}
	
	public UserVO selectUserInfoById(UserVO userVo) {
		return (UserVO) obj(prefix + "selectUserInfoById", userVo);
	}

	public CommonRequestStatusVO selectRequestStatus(RequestParam param) {
		return (CommonRequestStatusVO) obj(prefix + "selectRequestStatus", param);
	}

	public void insertDocsRequestFile(RequestParam param) {
		insert(prefix + "insertDocsRequestFile", param);
	}

	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}

	public List<ObjectInfoVO> selectDocInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectDocInspectionInfo", param);
	}
	public List<ObjectInfoVO> selectSimilarDocInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSimilarDocInspectionInfo", param);
	}

	public List<ObjectInfoVO> selectDrawingInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectDrawingInspectionInfo", param);
	}
	public List<ObjectInfoVO> selectSimilarDrawingInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSimilarDrawingInspectionInfo", param);
	}

	public List<ObjectInfoVO> selectSwInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSwInspectionInfo", param);
	}
	public List<ObjectInfoVO> selectSimilarSwInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSimilarSwInspectionInfo", param);
	}

	public List<ObjectInfoVO> selectProductDocInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectProductDocInspectionInfo", param);
	}
	public List<ObjectInfoVO> selectSimilarProductDocInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSimilarProductDocInspectionInfo", param);
	}

	public List<ObjectInfoVO> selectProductSwInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectProductSwInspectionInfo", param);
	}
	public List<ObjectInfoVO> selectSimilarProductSwInspectionInfo(ObjectInfoVO param) {
		return list(prefix + "selectSimilarProductSwInspectionInfo", param);
	}
	
	public void updateVendorAccept(RequestParam param) {
		update(prefix + "updateVendorAccept", param);
	}

	public void insertDocsRequestFileUsingMap(CommonDistributionRequestVO param) {
		insert(prefix + "insertDocsRequestFileUsingMap", param);
	}

}
