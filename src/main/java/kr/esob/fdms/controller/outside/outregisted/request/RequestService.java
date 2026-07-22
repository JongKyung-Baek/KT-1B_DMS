package kr.esob.fdms.controller.outside.outregisted.request;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;
import net.sf.json.JSONObject;

@Service
public class RequestService implements CommonService {

	@Inject
	RequestDao dao;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	DateUtil dateUtil;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public ResultVO saveUnregisterFile(MultipartHttpServletRequest request) throws IllegalStateException, IOException {
		ResultVO resultVo = new ResultVO();

		UnregisterPopupParam param = (UnregisterPopupParam) ObjectUtil.jsonToObj(request.getParameter("formUnRegisterPopup"), UnregisterPopupParam.class);

		//FDMS_UNREG_INFO insert
		dao.insertUnregisterInfo(param);
		//미등록 자료 파일 등록
		MultipartFile mf = request.getFile("file");
		String filePathNm = SystemConfig.getSystemConfigValue("UNREG_FILE_PATH") + param.getObjectNo() + "\\";
		File filePath = new File(filePathNm);
		if(!filePath.exists())filePath.mkdirs();
		param.setFilePath(filePathNm);
		String orgName = mf.getOriginalFilename();
		if(orgName.contains(File.separator)) {
			orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
		}
		
		orgName = StringUtil.replaceLfiPath(orgName);
		String path = filePathNm + orgName;
		path = StringUtil.replaceLfiPath(path);
		
		File file = new File(path);
		param.setFileNm(orgName);
		param.setFilePath(path);
		param.setFileSize(String.valueOf(mf.getSize()));
		mf.transferTo(file);
		dao.insertUnregisterFile(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO saveUnregisterFileX(UnregisterPopupParam param) throws Exception {
		ResultVO resultVo = new ResultVO();

		dao.insertUnregisterInfo(param);

		for (UnregisterPopupParam list : param.getParamList()) {
			list.setFileCd(param.getFileCd());
			dao.insertUnregisterFileX(list);
		}


		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO distributionRequest(DistributionRequestPopupParam param) throws UnsupportedEncodingException {
		ResultVO resultVo = new ResultVO();
		List<DistributionRequestPopupParam> arrNonProtect = new ArrayList<DistributionRequestPopupParam>();

		String rquestNo   = "";
		String fileFolder = "";
		
		param.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));							//오늘날짜 (YYYYMMDD)

		//유효기간 종료일자
		param.setUseEndYmd(dateUtil.getAddMonth(param.getDeployTerm(), "yyyyMMdd"));	//특정개월수더하기 (YYYYMMDD)

		//유효기간 (개월)
		param.setDeployTerm(param.getDeployTerm());

		param.setApprovalLineId("10");
		param.setCurrentProcessSeqNo("2");

		//INSERT DOCS_REQUEST
		dao.insertRequest(param);

		//INSERT DOCS_REQUEST_DEPLOY
		dao.insertRequestDeploy(param);

		@SuppressWarnings("unchecked")
		List<ApprovalLineDetailVO> appLindDetail = dao.getDocsApprovalLineDetail(param.getApprovalLineId());
		//INSERT DOCS_REQUEST_DETAIL
		for(int i=1; i<=appLindDetail.size(); i++) {
			param.setProcessSeq(Integer.toString(i));
			param.setApprovalStatusCd(appLindDetail.get(i-1).getApprovalStatusCd());
			param.setApprovalGradeCd(appLindDetail.get(i-1).getApprovalGradeCd());
			dao.insertDocsRequestDetail(param);
		}
		
		//INSERT DOCS_REQUEST_MAPPING
		for(DistributionRequestPopupParam vo : param.getList()) {
//				param.setObjectNo(vo);
			vo.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));							//오늘날짜 (YYYYMMDD)
			vo.setUseEndYmd(dateUtil.getAddMonth(param.getDeployTerm(), "yyyyMMdd"));	//특정개월수더하기 (YYYYMMDD)
			vo.setRequestNo(param.getRequestNo());
			List<DistributionRequestPopupParam> check = dao.selectRequestMappingDuplicate(vo);
			if(check.size() == 0) {
				dao.insertDocsRequestMapping(vo);
			}
			
			vo.setOrgFileNm(vo.getFileNm());
			JSONObject result = FileUtil.callSender(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE")
					, SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE")
					, vo.getFilePath().replaceAll("/", "\\\\")
					, SystemConfig.getSystemConfigValue("OUTREG_FILE_PATH")
					, vo.getFileNm());
			vo.setFilePath(SystemConfig.getSystemConfigValue("OUTREG_FILE_PATH") + result.getString("fileNm"));
			vo.setFileNm(result.getString("fileNm"));
			
			dao.insertDocsRequestFile(vo);
		}

		resultVo.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getReceiveUserCd());
				mailInfoVo.setMailEnum(DocsMailEnum.OUTREG_STATUS);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return resultVo;
		
	}
}
