package kr.esob.fdms.controller.inside.unregisted.request;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

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
// import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;

@Service
public class RequestService implements CommonService {

	@Inject
	UnregisterRequestDao dao;

//	@Inject
//	CommonRequestService commonRequestService;

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
		param.setFilePath(path);

		param.setFileNm(orgName);
		param.setFileSize(String.valueOf(mf.getSize()));
		mf.transferTo(file);
		dao.insertUnregisterFile(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO saveUnregisterFileX(UnregisterPopupParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		System.out.println("들어오는지 체크\n" + param.toString());
		//System.out.println(param.getParameterMap());
		// UnregisterPopupParam param = (UnregisterPopupParam) ObjectUtil.jsonToObj(param2.getParameter("formUnRegisterPopup"), UnregisterPopupParam.class);

		//FDMS_UNREG_INFO insert
		dao.insertUnregisterInfo(param);
		// dao.insertUnregisterInfo(param);
		for (UnregisterPopupParam list : param.getParamList()) {
			list.setFileCd(param.getFileCd());
			System.out.println("로그 확인중 -> " + list.getFileCd());
			dao.insertUnregisterFileX(list);
		}

//		MultipartFile mf = request.getFile("file");
//		System.out.println("asd --" + mf.getSize());
		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO saveUnregisterFileX2(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		System.out.println("들어오는지 체크 = " + request.getParameterMap());
		System.out.println("들어오는지 체크 = " + request.getParameter("fileName"));

		MultipartFile file = request.getFile("file");
		System.out.println("objectType -> "+request.getParameter("objectType"));
		// UnregisterPopupParam 객체 만들어서 저장
		UnregisterPopupParam unregisterPopupParam = UnregisterPopupParam.builder()
				// 자료명
				.dataName(request.getParameter("fileName"))
				.objectType(request.getParameter("objectType"))
				.fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
				// 파일명
//				.fileNm(request.getParameter("fileNm"))
				.fileNm(file.getName())
				.build();
		dao.insertUnregisterInfo(unregisterPopupParam);

		unregisterPopupParam = getFileSavedPath(unregisterPopupParam, file);
		dao.insertUnregisterFileX(unregisterPopupParam);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO distributionRequest(DistributionRequestPopupParam param) {
		ResultVO result = new ResultVO();
//		List<DistributionRequestPopupParam> arrNonProtect = new ArrayList<DistributionRequestPopupParam>();
		String securityEmailCC="";
		//보안팀 이메일 참조자 list
		if( null != param.getSecurityUserList() ) {
			for(String EmailCC : param.getSecurityUserList()) {
				if("".equals(securityEmailCC)) {
					securityEmailCC = EmailCC;
				}else {
					securityEmailCC += "," + EmailCC;
				}
			}
		}
		param.setSecurityEmailCC(securityEmailCC);

		String rquestNo = "";
		param.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));                            //오늘날짜 (YYYYMMDD)

		//배포유형 (체크박스)
		if("general".equals(param.getFileDistributionType())) {
			param.setDeployNormalYn("Y");
			param.setDeploySpecialYn("N");
		}else if("security".equals(param.getFileDistributionType())) {
			param.setDeployNormalYn("N");
			param.setDeploySpecialYn("Y");
		}else {
			param.setDeployNormalYn("N");
			param.setDeploySpecialYn("N");
		}

		//유효기간 종료일자
		param.setUseEndYmd(dateUtil.getAddMonth(param.getDeployTerm(), "yyyyMMdd"));    //특정개월수더하기 (YYYYMMDD)

		//유효기간 (개월)
		param.setDeployTerm(param.getDeployTerm());

//		if(arrNonProtect.size()>0) {	//요청이 있을경우 (방산x)
		param.setApprovalLineId("2");
		param.setCurrentProcessSeqNo("3");

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
			vo.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));                            //오늘날짜 (YYYYMMDD)
			vo.setUseEndYmd(dateUtil.getAddMonth(param.getDeployTerm(), "yyyyMMdd"));    //특정개월수더하기 (YYYYMMDD)
			vo.setRequestNo(param.getRequestNo());
			List<DistributionRequestPopupParam> check = dao.selectRequestMappingDuplicate(vo);
			if(check.size() == 0) {
				dao.insertDocsRequestMapping(vo);
			}
			dao.insertDocsRequestFile(vo);
		}
//		}
		result.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getTeamLeader());
				mailInfoVo.setMailEnum(DocsMailEnum.UNREG_APPROVAL);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private UnregisterPopupParam getFileSavedPath(UnregisterPopupParam param, MultipartFile mf){
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
		param.setFilePath(path);
		System.out.println("파일 경로 : " + path);

		param.setFileNm(orgName);
		param.setFileSize(String.valueOf(mf.getSize()));

		try {
			file.createNewFile();

			// 0703 (yskim)
			byte[] bytes = mf.getBytes();
			Files.write(Paths.get(path), bytes);
			//

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return param;
	}
}
