package kr.esob.fdms.controller.inside.cr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;

@Service
public class CommonCrService {
	@Inject
	CommonCrDao commonCrDao;

	public CrFileVO selectInsideFilePath(CrFileVO param) {
		return commonCrDao.selectInsideFilePath(param);
	}

	public CrFileVO selectOutsideFilePath(CrFileVO param) {
		return commonCrDao.selectOutsideFilePath(param);
	}

	public void crFileDownload(CrFileVO param, HttpServletResponse response) throws Exception {
		File file = new File(param.getDocsFilePathNm());
		String mimeType = URLConnection.guessContentTypeFromName(param.getOrgFileNm());
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + param.getOrgFileNm() + "\""));
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

}


