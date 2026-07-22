package kr.esob.fdms.controller.outside.commondestroyrequest;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.inside.distribution.history.DestroyPopupParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.cr.request.OutsideCrParam;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.ObjectUtil;
import net.sf.json.JSONObject;

@Service
public class CommonDestroyRequestService {

	@Inject
	CommonDestroyRequestDao dao;
	@Inject
	CommonApprovalDao commonApprovalDao;
	@Inject
	DocsMailService mailService;
	
	@SuppressWarnings("unchecked")
	public ResultVO insertDestory(MultipartHttpServletRequest request) throws Exception {

		ResultVO resultVo = new ResultVO();
//		List<DestroyPopupParam> destroyList = new Gson().fromJson(request.getParameter("list"), new TypeToken<List<DestroyPopupParam>>() {}.getType());
		List<DestroyPopupParam> destroyList = (List<DestroyPopupParam>) ObjectUtil.jsonArrToList(request.getParameter("list"), DestroyPopupParam.class);

		System.out.println("destroyList 크기: " + destroyList.size());
		for(DestroyPopupParam destroyPopupParam : destroyList){
			System.out.println("destroyPopupParam : " + destroyPopupParam);
		}

		DestroyPopupParam param = (DestroyPopupParam) ObjectUtil.jsonToObj(request.getParameter("destroyParam"), DestroyPopupParam.class);
//		param.setDestroyDesc(request.getParameter("destroyDesc"));
		param.setBusinessAreaCd(destroyList.get(0).getBusinessAreaCd());
		//FDMS_DESTROY insert
		dao.insertDestroyInfo(param);
		//폐기 첨부파일 저장
		Iterator<String> itr = request.getFileNames();
		String filePathNm = SystemConfig.getSystemConfigValue("DESTROY_FILE_PATH") + param.getDestroyNo() + "\\";
		File filePath = new File(filePathNm);
		if(!filePath.exists())filePath.mkdirs();
//		param.setFilePath(filePathNm);
		if(itr.hasNext()) {
			List<MultipartFile> list = request.getFiles(itr.next().toString());
//			crParam.setFileList(list);
			int i = 1;			// FILE SEQUENCE
			for(MultipartFile multipartFile : list) {
				param.setDestroyFileSeq(i);
				String orgName = multipartFile.getOriginalFilename();
				if(orgName.contains("\\")) {
					orgName = orgName.substring(orgName.lastIndexOf("\\")+1, orgName.length());
				}
				File file = new File(filePathNm + orgName);
				param.setFileNm(orgName);
				param.setFilePath(filePathNm + orgName);
//				File file = new File(filePathNm + multipartFile.getOriginalFilename());
				multipartFile.transferTo(file);
				
				JSONObject result = FileUtil.callSender( 
						SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE")
						,SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE")
						,filePathNm + orgName
						,SystemConfig.getSystemConfigValue("DESTROY_FILE_PATH") + param.getDestroyNo() + "\\"
						, orgName);
				param.setFilePathInside(SystemConfig.getSystemConfigValue("DESTROY_FILE_PATH") + param.getDestroyNo() + "\\" + result.getString("fileNm"));
				dao.insertDestroyFile(param);
				i++;
			}
		}

		String approvalLineId = "8";

		param.setApprovalLineId(approvalLineId);
		// 폐기 insert
		dao.insertDestroyRequest(param);

		List<ApprovalLineDetailVO> appLinedDetail = commonApprovalDao.getDocsApprovalLineDetail(approvalLineId);

		if (appLinedDetail != null && !appLinedDetail.isEmpty()) {
			for (ApprovalLineDetailVO detail : appLinedDetail) {
				// 각 ApprovalLineDetailVO 객체의 필드 값 출력
				System.out.println("Approval Status Code: " + detail.getApprovalStatusCd());
				System.out.println("Approval Grade Code: " + detail.getApprovalGradeCd());
			}
		} else {
			System.out.println("Approval Line Detail list is empty or null.");
		}

		// 결재 라인 조회
		for(int i=0; i<appLinedDetail.size(); i++) {
			CommonApprovalParam tmp = new CommonApprovalParam();

			tmp.setProcessSeq(Integer.toString(i+1));
			tmp.setApprovalStatusCd(appLinedDetail.get(i).getApprovalStatusCd());
			tmp.setDestroyRequestNo(param.getDestroyRequestNo());
			tmp.setApprovalUser(param.getApprovalUser());
			tmp.setActionCd(Constant.REQUEST);
//				if(0 == i) {
//				}

			dao.insertDestroyRequestDetail(tmp);
		}
		// 폐기 사유
		dao.updateDestroyRequestDesc(param);

		for(DestroyPopupParam tempParam : destroyList) {
			tempParam.setDestroyRequestNo(param.getDestroyRequestNo());
			tempParam.setDestroyNo(param.getDestroyNo());
			tempParam.setDestroyDesc(param.getDestroyDesc());
			dao.updateRequestFileDestroy(tempParam);
			dao.insertDestroyRequestMapping(tempParam);
			dao.updateApprovalFile(tempParam);
		}

		try {
			sendMail(param);
		}catch(Exception e) {
			e.printStackTrace();
		}
		resultVo.setSuccess(true);
		return resultVo;
	}
	
	private void sendMail(DestroyPopupParam param) {
		MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getApprovalUser());
		mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_DISPOSAL_OUTSIDE);
		mailService.sendDocsMail(mailInfoVo);
	}
	
	
	public ResultVO updateUnderDestroy(DestroyRequestParam param) {
		ResultVO resultVo = new ResultVO();
		dao.updateUnderDestroy(param);
		resultVo.setSuccess(true);
		return resultVo;
	}
}
