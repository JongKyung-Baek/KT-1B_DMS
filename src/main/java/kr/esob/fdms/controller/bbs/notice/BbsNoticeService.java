package kr.esob.fdms.controller.bbs.notice;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;
import kr.esob.fdms.util.StoragePathUtils;
import net.sf.json.JSONObject;

@Service
public class BbsNoticeService implements CommonService{

	@Inject
	BbsNoticeDao dao;

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

	/**
	 * 메인화면용
	 * @param param
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List selectMainList(Object param) {
		return dao.selectMainList(param);
	}

	public BbsNoticePopupVO getRequestInfo(BbsNoticePopupParam param) {
		return dao.getRequestInfo(param);
	}

	public Integer updateHitCount(Object param) {
		return dao.updateHitCount(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO insertBbsNotice(MultipartHttpServletRequest request) throws IllegalStateException, IOException {
		ResultVO resultVo = new ResultVO();
		BbsNoticePopupParam bbsParam = (BbsNoticePopupParam) ObjectUtil.jsonToObj(request.getParameter("bbsParam"), BbsNoticePopupParam.class);

		bbsParam.setNoticeTitle(StringUtil.encodeHTML(bbsParam.getNoticeTitle()));
		bbsParam.setContents(StringUtil.encodeHTML(bbsParam.getContents()));

		dao.insertNoticeInfo(bbsParam);

		Iterator<String> itr = request.getFileNames();
		MultipartFile mf = request.getFile("file");
		File filePath = StoragePathUtils.resolve(
				SystemConfig.getSystemConfigValue("NOTICE_FILE_PATH"),
				String.valueOf(bbsParam.getNoticeCd())).toFile();
		if(!filePath.exists())filePath.mkdirs();
		if(itr.hasNext()) {
			bbsParam.setFilePath(filePath.getPath());
			String orgName = StoragePathUtils.fileName(mf.getOriginalFilename());
			File file = StoragePathUtils.resolve(filePath.getPath(), orgName).toFile();
			bbsParam.setFilePath(file.getPath());
			bbsParam.setFileNm(orgName);
			bbsParam.setFileSize(String.valueOf(mf.getSize()));
			mf.transferTo(file);

			@SuppressWarnings("deprecation")
			JSONObject result = FileUtil.callSender(
					FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE"))
					,FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE"))
					,FileUtil.encryptTransferArgument(file.getPath())
					,FileUtil.encryptTransferArgument(filePath.getPath())
					,FileUtil.encryptTransferArgument(orgName));
			String transferredFileName = FileUtil.requireSuccessfulTransferFileName(result);
			bbsParam.setFilePathOutside(
					StoragePathUtils.resolve(filePath.getPath(), transferredFileName).toString());
			dao.insertNoticeFile(bbsParam);
		}
		resultVo.setSuccess(true);
		return resultVo;
	}

	public BbsNoticePopupParam selectNoticeInfo(BbsNoticePopupParam param) {
		return dao.selectNoticeInfo(param);
	}

	public ResultVO saveNotice(BbsNoticePopupParam param) {
		ResultVO result = new ResultVO();
		dao.deleteNotice(param);
		result.setSuccess(true);
		return result;
	}

	public BbsNoticePopupVO addNoticeInfo(BbsNoticeAddParam param) {
		UserVO userVo = new UserVO();
		param.setInsertUserNm(userVo.getUsername());
		return dao.addNoticeInfo(param);
	}

	public void deleteNoticeFile(BbsNoticePopupParam param) {

		BbsNoticeFileVO vo = new BbsNoticeFileVO();
		vo.setNoticeCd(param.getNoticeCd());

		List<BbsNoticeFileVO> list = dao.selectFileList(vo);

		if(list.size() > 0) {
			for(BbsNoticeFileVO info : list) {
				File f = new File(info.getFilePath());
				if(f.exists()) {
					f.delete();
				}
			}
		}

		dao.deleteNoticeFile(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO updateNoticeInfo(MultipartHttpServletRequest request) throws IllegalStateException, IOException {
		ResultVO resultVo = new ResultVO();
		BbsNoticePopupParam bbsParam = (BbsNoticePopupParam) ObjectUtil.jsonToObj(request.getParameter("bbsParam"), BbsNoticePopupParam.class);

		bbsParam.setNoticeTitle(StringUtil.encodeHTML(bbsParam.getNoticeTitle()));
		bbsParam.setContents(StringUtil.encodeHTML(bbsParam.getContents()));

		dao.updateNoticeInfo(bbsParam);

		Iterator<String> itr = request.getFileNames();
		MultipartFile mf = request.getFile("file");
		File filePath = StoragePathUtils.resolve(
				SystemConfig.getSystemConfigValue("NOTICE_FILE_PATH"),
				String.valueOf(bbsParam.getNoticeCd())).toFile();
//		"NOTICE_FILE_PATH" = C:\DOCS\NOTICE\
		
		if(!filePath.exists())filePath.mkdirs();
		if(itr.hasNext()) {
			bbsParam.setFilePath(filePath.getPath());
			String orgName = StoragePathUtils.fileName(mf.getOriginalFilename());
			File file = StoragePathUtils.resolve(filePath.getPath(), orgName).toFile();
			bbsParam.setFilePath(file.getPath());
			
			bbsParam.setFileNm(orgName);
			bbsParam.setFileSize(String.valueOf(mf.getSize()));
			mf.transferTo(file);

			JSONObject result = FileUtil.callSender(
					FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE"))
					,FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE"))
					,FileUtil.encryptTransferArgument(file.getPath())
					,FileUtil.encryptTransferArgument(filePath.getPath())
					,FileUtil.encryptTransferArgument(orgName));
			String transferredFileName = FileUtil.requireSuccessfulTransferFileName(result);
			bbsParam.setFilePathOutside(
					StoragePathUtils.resolve(filePath.getPath(), transferredFileName).toString());
			// Delete only the old DB row after the replacement has reached the
			// remote server. Physical cleanup cannot participate in the DB
			// transaction and is intentionally deferred to avoid losing the old
			// file when transfer validation fails.
			dao.deleteNoticeFile(bbsParam);
			dao.insertNoticeFile(bbsParam);
		}
		
		resultVo.setSuccess(true);
		return resultVo;
	}

//	public ResultVO updateNoticeInfo(BbsNoticePopupParam param) {
//		ResultVO resultVo = new ResultVO();
//
//		param.setNoticeTitle(StringUtil.encodeHTML(param.getNoticeTitle()));
//		param.setContents(StringUtil.encodeHTML(param.getContents()));
//
//		dao.updateNoticeInfo(param);
//		resultVo.setSuccess(true);
//		return resultVo;
//	}

	public BbsNoticePopupVO selectNoticePopupInfo(BbsNoticePopupParam param) {
		BbsNoticePopupVO vo = dao.selectNoticePopupInfo(param);
		vo.setFileList(dao.selectNoticeFileList(param));
		return vo;
	}

	public void fileDownload(BbsNoticeFileVO param, HttpServletResponse response, HttpServletRequest request) throws IOException {
		BbsNoticeFileVO vo = dao.selectFilePath(param);

		File file = null;

		if (param.getSessionUser().getAuthSite().equals("I")) {
			file = new File(vo.getFilePath());
		}
		else {
			file = new File(vo.getFilePathOutside());
		}

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

		} else if (browser.equals("Trident")) {

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
//		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + vo.getFileNm() + "\""));
		response.setContentLength((int) file.length());
		
		try {
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();
            response.getOutputStream().close();
        } catch (FileNotFoundException ex) {
        	throw new FileNotFoundException();
        } catch (IOException ex) {
        	throw new IOException();
        }
	}

	public BbsNoticePopupVO selectAlertNotice() {
		return dao.selectAlertNotice();
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
}
