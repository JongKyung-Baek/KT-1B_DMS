package kr.esob.fdms.controller.inside.cr.approval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrFileVO;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.util.FileUtil;
import net.sf.json.JSONObject;

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
		CrInfoVO vo = dao.selectApprovalInfo(param);
		vo.setFileList(commonCrDao.selectInsideFileList(param));
		return vo;
	}

	public ResultVO approve(CrParam param) throws IOException {
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.APPROVAL);
		param.setApprovalStatusCd(Constant.APPROVAL);
//		param.setRequestDesc(param.getReviewResult());
//		dao.updateAcceptance(param);
		param.setStatusCd(CrStatusCdInfo.TEAMLEADER_APPROVAL);
		dao.updateRequest(param);
		dao.updateRequestDetail(param);
		dao.updateCr(param);
		copyFile(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public void copyFile(CrParam param) throws IOException {
		List<CrFileVO> crFileList = commonCrDao.selectInsideFileList(param);
		String interfacePath = SystemConfig.getSystemConfigValue("CR_INTERFACE_PATH");
		String interfaceDbPath = SystemConfig.getSystemConfigValue("CR_INTERFACE_DB_PATH");
		for(CrFileVO fileInfo : crFileList) {
			String targetPath = interfacePath + fileInfo.getCrNo().substring(2, 6);
			FileUtils.copyFileToDirectory(new File(fileInfo.getDocsFilePathNm()), new File(targetPath));
			fileInfo.setFilePathNm(interfaceDbPath + fileInfo.getCrNo().substring(2, 6) + "\\" + fileInfo.getFileNm());
			dao.updateCrFile(fileInfo);
		}
	}

	public ResultVO approvalReject(CrParam param) {
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.REJECT);
		param.setRequestDesc(param.getRejectReason());
		param.setStatusCd(CrStatusCdInfo.TEAMLEADER_REJECT);
		dao.updateRequest(param);
		dao.updateRequestDetail(param);
		dao.updateCr(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

}
