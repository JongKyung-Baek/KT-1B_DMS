package kr.esob.fdms.controller.outside.commondestroystatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class CommonDestroyStatusService {

	@Inject
	CommonDestroyStatusDao dao;

	public DestroyStatusInfoVO selectDestroyStatus(DestroyStatusParam param) {
		DestroyStatusInfoVO vo = dao.selectDestroyStatus(param);
		vo.setFileList(dao.selectDestroyFileList(vo));
		return vo;
	}

	public void destroyFileDown(DestroyFileVO param, HttpServletResponse response) throws Exception{
		DestroyFileVO vo = dao.selectFilePath(param);
		File file = new File(vo.getFilePath());
		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		if (mimeType == null) {
			mimeType = "image/*";
//			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);
		response.setHeader("X-Content-type-options", String.format("disabled=true"));
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
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
