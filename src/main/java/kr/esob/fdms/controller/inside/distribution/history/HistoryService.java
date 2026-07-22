package kr.esob.fdms.controller.inside.distribution.history;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.bbs.notice.BbsNoticeAddParam;
import kr.esob.fdms.controller.outside.commondestroyrequest.DestroyRequestParam;
import kr.esob.fdms.controller.outside.doc.request.DocInfoVO;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import kr.esob.fdms.controller.outside.sw.request.SwInfoVO;
import kr.esob.fdms.util.ObjectUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class HistoryService implements CommonService{

	@Inject
	HistoryDao dao;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public List selectCompanyList(stopDealPopupParam param) {
		return dao.selectCompanyList(param);
	}

	public int selectCompanyListCount(stopDealPopupParam param) {
		return dao.selectCompanyListCount(param);
	}

	public ResultVO deleteCompany(CompanyListVO param) {
		ResultVO result = new ResultVO();
		for(CompanyListVO vo : param.getList()) {
			dao.deleteCompany(vo);
			dao.deleteUser(vo);
			dao.updateRequestFileDestroy(vo);
			dao.deleteVendor(vo);
			if(vo.getSendEmailYn().isBooleanValue()) {
				List<MailInfoVO> mailInfoVoList = mailService.selectCompanyUserList(vo);
				if(mailInfoVoList.size() > 0) {
					for(MailInfoVO mailInfoVo : mailInfoVoList) {
						if(null == mailInfoVo) {
							continue;
						}
						mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_DELETE_COMPANY);
						mailService.sendDocsMail(mailInfoVo);
					}
				}
			}
		}

		result.setSuccess(true);
		return result;
	}

	public ResultVO insertDestory(MultipartHttpServletRequest request) throws Exception {

		@SuppressWarnings({ "deprecation", "unchecked" })
		List<DestroyPopupParam> destroyList = JSONArray.toList(JSONArray.fromObject(request.getParameter("list")), DestroyPopupParam.class);
		ResultVO resultVo = new ResultVO();
		DestroyPopupParam destroyInfo = (DestroyPopupParam) ObjectUtil.jsonToObj(request.getParameter("destroyParam"), DestroyPopupParam.class);
		DestroyPopupParam param = new DestroyPopupParam();
//		param.setDestroyDesc(request.getParameter("destroyDesc"));

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
				param.setFilePath(filePathNm + orgName);
//				File file = new File(filePathNm + multipartFile.getOriginalFilename());
				multipartFile.transferTo(file);
				dao.insertDestroyFile(param);
				i++;
			}
		}


		for(DestroyPopupParam tempParam : destroyList) {
			tempParam.setDestroyNo(param.getDestroyNo());
			// 폐기 완료
			dao.updateDestroyRequest(tempParam);
			// 폐기 사유
			tempParam.setDestroyDesc(destroyInfo.getDestroyDesc());
			dao.updateDestroyRequestDesc(tempParam);
		}

		resultVo.setSuccess(true);
		return resultVo;
	}

	public void setSearchAllParam(HistoryListParam param) {
		if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
			Gson gson = new Gson();
			param.setDrawingList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DrawingInfoVO>>() {}.getType()));
			param.setDocumentList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DocInfoVO>>() {}.getType()));
			param.setSwList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<SwInfoVO>>() {}.getType()));
		}
	}

	public ResultVO updateUnderDestroy(HistoryListVO param) {
		ResultVO resultVo = new ResultVO();
		dao.updateUnderDestroy(param);
		resultVo.setSuccess(true);
		return resultVo;
	}
}
