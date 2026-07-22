package kr.esob.fdms.controller.configurationmanage.qna;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;

@Service
public class ConfigQnaService implements CommonService{

	@Inject
	ConfigQnaDao dao;

	@Inject
	DateUtil dateUtil;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}
	
	public ResultVO insertConfigQna(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		ConfigQnaAddParam configParam = (ConfigQnaAddParam) ObjectUtil.jsonToObj(request.getParameter("configParam"), ConfigQnaAddParam.class);
		
		configParam.setTitle(StringUtil.encodeHTML(configParam.getTitle()));
		configParam.setContents(StringUtil.encodeHTML(configParam.getContents()));
		
		dao.insertQna(configParam);
		
		Iterator<String> itr = request.getFileNames();
		MultipartFile mf = request.getFile("file");
		String filePathNm = SystemConfig.getSystemConfigValue("CON_QNA_FILE_PATH") + configParam.getQnaCd() + "\\";
		File filePath = new File(filePathNm);
		if(!filePath.exists()) filePath.mkdirs();
		if(itr.hasNext()) {
			String orgName = mf.getOriginalFilename();
			if(orgName.contains(File.separator)) {
				orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
			}
			orgName = StringUtil.replaceLfiPath(orgName);
			String path = filePathNm + orgName;
			path = StringUtil.replaceLfiPath(path);
			File file = new File(path);
			configParam.setFilePath(path);
			configParam.setFileNm(orgName);
			configParam.setOrgFileNm(orgName);
			mf.transferTo(file);

			dao.insertConfigQnaFile(configParam);
		}
		resultVo.setSuccess(true);
		return resultVo;
	}
	
	public ConfigQnaPopupVO getQnaInfo(ConfigQnaPopupVO param) {
		return dao.getQnaInfo(param);
	}
	
	public ConfigQnaPopupVO selectQnaFileInfo(ConfigQnaPopupVO param) {
		param.setFileList(dao.selectQnaFileList(param));
		return param;
	}
	
	public ResultVO updateConfigQna(MultipartHttpServletRequest request) throws IllegalStateException, IOException {
		ResultVO resultVo = new ResultVO();
		ConfigQnaAddParam configParam = (ConfigQnaAddParam) ObjectUtil.jsonToObj(request.getParameter("configParam"), ConfigQnaAddParam.class);

		configParam.setTitle(StringUtil.encodeHTML(configParam.getTitle()));
		configParam.setContents(StringUtil.encodeHTML(configParam.getContents()));

		dao.updateConfigQna(configParam);

		Iterator<String> itr = request.getFileNames();
		MultipartFile mf = request.getFile("file");
		String filePathNm = SystemConfig.getSystemConfigValue("CON_QNA_FILE_PATH") + configParam.getQnaCd() + "\\";
		File filePath = new File(filePathNm);
		if(!filePath.exists()) filePath.mkdirs();
		
		if(configParam.getFileDelete().equals("true")) {
//			 기존 파일 삭제
			deleteQnaFile(configParam);
		}
		
		if(itr.hasNext()) {
			// 기존 파일 삭제
			deleteQnaFile(configParam);
			
			String orgName = mf.getOriginalFilename();
			if(orgName.contains(File.separator)) {
				orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
			}
			orgName = StringUtil.replaceLfiPath(orgName);
			
			String path = filePathNm + orgName;
			path = StringUtil.replaceLfiPath(path);
			
			File file = new File(path);
			configParam.setFilePath(path);
			configParam.setFileNm(orgName);
			configParam.setOrgFileNm(orgName);
			mf.transferTo(file);

			dao.insertConfigQnaFile(configParam);
		}
		resultVo.setSuccess(true);
		return resultVo;
	}
	
	public void deleteQnaFile(ConfigQnaAddParam param) {
		List<ConfigQnaAddParam> list = dao.selectFileList(param);
		if(list.size() > 0) {
			for(ConfigQnaAddParam info : list) {
				File f = new File(info.getFilePath());
				if(f.exists()) {
					f.delete();
				}
			}
		}

		dao.deleteQnaFile(param);
	}
	
	@SuppressWarnings("deprecation")
	public void fileDownload(ConfigQnaPopupVO param, HttpServletResponse response, HttpServletRequest request) throws IOException {
		ConfigQnaPopupVO vo = dao.selectFileInfo(param);
		File file = new File(vo.getFilePath());
		String mimeType = URLConnection.guessContentTypeFromName(vo.getFileNm());
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		String browser = getBrowser(request);
        String dispositionPrefix = "attachment; fileName=";
        String encodedfileName = null;
        String fileName = vo.getFileNm();
		if (browser.equals("MSIE")) {

			encodedfileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

		} else if (browser.equals("Trident")) { // IE11 문자열 깨짐 방지

			encodedfileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

		} else if (browser.equals("Firefox")) {

			encodedfileName = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";

			encodedfileName = URLDecoder.decode(encodedfileName);

		} else if (browser.equals("Opera")) {

			encodedfileName = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";

		} else if (browser.equals("Chrome")) {

			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < fileName.length(); i++) {

				char c = fileName.charAt(i);

				if (c > '~') {

					sb.append(URLEncoder.encode("" + c, "UTF-8"));

				} else {

					sb.append(c);

				}

			}

			encodedfileName = sb.toString();

		} else if (browser.equals("Safari")) {

			encodedfileName = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";

			encodedfileName = URLDecoder.decode(encodedfileName);

		}

		else {

			encodedfileName = "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", dispositionPrefix+encodedfileName);
		response.setContentLength((int) file.length());
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (FileNotFoundException ex) {
        	throw new FileNotFoundException();
        } catch (IOException ex) {
        	throw new IOException();
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
	
	public String getBrowser(HttpServletRequest request) {

		String header = request.getHeader("User-Agent");

		if (header.indexOf("MSIE") > -1) {

			return "MSIE";

		} else if (header.indexOf("Trident") > -1) { // IE11 문자열 깨짐 방지

			return "Trident";

		} else if (header.indexOf("Chrome") > -1) {

			return "Chrome";

		} else if (header.indexOf("Opera") > -1) {

			return "Opera";

		} else if (header.indexOf("Safari") > -1) {

			return "Safari";

		}

		return "Firefox";

	}
	
	public ResultVO replyQna(MultipartHttpServletRequest request) {
		ResultVO resultVo = new ResultVO();
		
		ConfigQnaPopupVO replyParam = (ConfigQnaPopupVO) ObjectUtil.jsonToObj(request.getParameter("replyParam"), ConfigQnaPopupVO.class);
		replyParam.setAnswer(StringUtil.encodeHTML(replyParam.getAnswer()));
		
		dao.replyQna(replyParam);

		resultVo.setSuccess(true);
		return resultVo;
	}
	
	public ResultVO saveQna(ConfigQnaPopupVO param) {
		ResultVO result = new ResultVO();
		dao.deleteQna(param);
		result.setSuccess(true);
		return result;
	}
}