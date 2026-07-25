package kr.esob.fdms.controller.inside.cr.approval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrFileVO;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.StoragePathUtils;

@Service
public class ApprovalService implements CommonService {

	@Inject
	ApprovalDao dao;

	@Inject
	CommonCrDao commonCrDao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

//	public void updateList(Object param) {
//		dao.updateList(param);
//	}

	public void deleteList(Object param) {

	}

	public CrInfoVO selectApprovalInfo(CrParam param) {
		param.setSessionUser(requireAuthenticatedActor());
		CrInfoVO vo = dao.selectApprovalInfo(param);
		if (vo == null) {
			throw new AccessDeniedException("CR approval request is not accessible");
		}
		vo.setFileList(commonCrDao.selectInsideFileList(param));
		return vo;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO approve(CrParam param) throws IOException {
		ResultVO resultVo = new ResultVO();
		authorizeApprovalTarget(param);
		param.setActionCd(Constant.APPROVAL);
		param.setApprovalStatusCd(Constant.APPROVAL);
//		param.setRequestDesc(param.getReviewResult());
//		dao.updateAcceptance(param);
		param.setStatusCd(CrStatusCdInfo.TEAMLEADER_APPROVAL);
		requireSingleRow(dao.updateRequest(param), "approve CR request");
		requireSingleRow(dao.updateRequestDetail(param), "complete CR approval step");
		requireSingleRow(dao.updateCr(param), "update approved CR");
		copyFile(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public void copyFile(CrParam param) throws IOException {
		List<CrFileVO> crFileList = commonCrDao.selectInsideFileList(param);
		String interfacePath = SystemConfig.getSystemConfigValue("CR_INTERFACE_PATH");
		String interfaceDbPath = SystemConfig.getSystemConfigValue("CR_INTERFACE_DB_PATH");
		for(CrFileVO fileInfo : crFileList) {
			String crFolder = fileInfo.getCrNo().substring(2, 6);
			File targetDirectory = StoragePathUtils.resolve(interfacePath, crFolder).toFile();
			FileUtils.copyFileToDirectory(
					StoragePathUtils.toPath(fileInfo.getDocsFilePathNm()).toFile(),
					targetDirectory);
			fileInfo.setFilePathNm(StoragePathUtils.resolve(
					interfaceDbPath,
					crFolder + "/" + StoragePathUtils.fileName(fileInfo.getFileNm())).toString());
			requireSingleRow(dao.updateCrFile(fileInfo), "update approved CR file");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO approvalReject(CrParam param) {
		ResultVO resultVo = new ResultVO();
		authorizeApprovalTarget(param);
		param.setActionCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.REJECT);
		param.setRequestDesc(param.getRejectReason());
		param.setStatusCd(CrStatusCdInfo.TEAMLEADER_REJECT);
		requireSingleRow(dao.updateRequest(param), "reject CR request");
		requireSingleRow(dao.updateRequestDetail(param), "complete CR rejection step");
		requireSingleRow(dao.updateCr(param), "update rejected CR");
		resultVo.setSuccess(true);
		return resultVo;
	}

	private void authorizeApprovalTarget(CrParam param) {
		if (param == null || param.getCrNo() == null || param.getCrNo().trim().isEmpty()) {
			throw new IllegalArgumentException("CR number is required");
		}
		param.setCrNo(param.getCrNo().trim());
		param.setSessionUser(requireAuthenticatedActor());
		if (dao.selectApprovalTargetForUpdate(param) == null) {
			throw new AccessDeniedException("CR approval request is not accessible");
		}
	}

	private UserVO requireAuthenticatedActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
			throw new AccessDeniedException("Authenticated user is required");
		}
		return (UserVO) authentication.getPrincipal();
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

}
