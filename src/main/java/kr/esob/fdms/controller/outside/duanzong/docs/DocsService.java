package kr.esob.fdms.controller.outside.duanzong.docs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import net.sf.json.JSONObject;

@Service
public class DocsService implements CommonService {

	@Inject
	DocsMailService mailService;
	
	@Inject
	DocsDao dao;

	@Inject
	CommonRequestDao requestDao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}
	
	public DocsStatusPopupVO getDuanzongDocs(String managementNo) {
		return dao.getDuanzongDocs(managementNo);
	}
		
	public List<DocsDefensePursVO> getDefensePurs() {
		return dao.getDefensePurs();
	}
	
	public ResultVO insertDocsDuanzong(MultipartHttpServletRequest request) throws IllegalStateException, IOException {
		ResultVO resultVo = new ResultVO();
		
		Integer no = dao.managementNoMax();
		String number = ""+no;
		if(no < 10) number = "000" + no;
		else if(no < 100) number = "00" + no;
		else if(no < 1000) number = "0" + no;
		
		SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd");
		Date nowDate = new Date();
		String yyyy = dbFormat.format(nowDate);
		DocsAddParam docParam = (DocsAddParam) ObjectUtil.jsonToObj(request.getParameter("docParam"), DocsAddParam.class);
		String managementNo = "DOCS_" + yyyy + "_" + number;
		docParam.setManagementNo(managementNo);
		
		MailInfoVO mailInfoVo = mailService.selectReceiveUser(docParam.getDefensePurNm());
		mailInfoVo.setMailEnum(DocsMailEnum.DUANZONG_DOCS);
		mailService.sendDocsMail(mailInfoVo);
		
		DocsDefensePursUserVO user = dao.getDefensePursUser(docParam.getDefensePurNm());
		docParam.setDefensePurId(user.getUserId());
		docParam.setDefensePurNm(user.getUserNm());
		
		Iterator<String> itr = request.getFileNames();
		MultipartFile mf = request.getFile("file");
		
		String fileDate = fileFormat.format(nowDate);
		String fileInPathNm = SystemConfig.getSystemConfigValue("DUANZONG_FILE_PATH_E") + "\\" + fileDate + "\\";
		String fileOutPathNm = SystemConfig.getSystemConfigValue("DUANZONG_FILE_PATH") + "\\" + fileDate + "\\";
		File fileInPath = new File(fileInPathNm);
		File fileOutPath = new File(fileOutPathNm);
		if(!fileInPath.exists()) fileInPath.mkdirs();
		if(!fileOutPath.exists()) fileOutPath.mkdirs();
		if(itr.hasNext()) {
			String orgName = mf.getOriginalFilename();
			if(orgName.contains(File.separator)) {
				orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
			}
			orgName = StringUtil.replaceLfiPath(orgName);
			
			String name = docParam.getManagementNo() + "_" + orgName;
			name = StringUtil.replaceLfiPath(name);
			
			String path = fileInPathNm + name;
			path = StringUtil.replaceLfiPath(path);
			
			File inFile = new File(path);
			mf.transferTo(inFile);
			
			String srcUrl = Seed128Cipher.encrypt(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			String dstUrl = Seed128Cipher.encrypt(SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			String srcFilePath = Seed128Cipher.encrypt(path, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			String dstFilePath = Seed128Cipher.encrypt(fileOutPathNm, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			String dstFileNm = Seed128Cipher.encrypt(name, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			
			JSONObject result = FileUtil.callSender(srcUrl, dstUrl, srcFilePath, dstFilePath, dstFileNm);
			
			docParam.setFileNm(name);
			docParam.setFilePath(path);
			docParam.setFilePathE(fileOutPathNm + result.getString("fileNm"));

			dao.insertDocsDuanzong(docParam);
		}
		
		resultVo.setSuccess(true);
		return resultVo;
	}
	
	public void docsFileDownload(DocsStatusPopupVO param, HttpServletResponse response) throws Exception {
//		기본설정 내부사용자 경로
		UserVO user = param.getSessionUser();
		String path = param.getFilePath();
		String filePathNm = param.getFileNm();
		
//		외부사용자 경로
		if(user.getAuthLevel().equals("2")) {
			path = param.getFilePathE();
			filePathNm = path;
		}
		
		File file = new File(path);
		String mimeType = URLConnection.guessContentTypeFromName(filePathNm);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + param.getFileNm() + "\""));
		response.setContentLength((int) file.length());
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch(Exception e){
            e.printStackTrace();
        } finally {
        	if(inputStream != null) {
        		try {
        			inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        	if(response != null) {
        		response.getOutputStream().close();
        	}
		}
	}
}