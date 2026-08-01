package kr.esob.tdms.controller.general.cr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLConnection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.server.ResponseStatusException;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class CommonCrService {
	@Inject
	CommonCrDao commonCrDao;

	@Inject
	SecurityAclService securityAclService;

	public void downloadInside(CrFileDownloadParam param, HttpServletResponse response) throws IOException {
		download(commonCrDao.selectInsideDownloadResource(trusted(param)), response);
	}

	private CrFileDownloadParam trusted(CrFileDownloadParam param) {
		if (param == null || blank(param.getCrNo()) || param.getFileNo() <= 0) {
			throw notFound();
		}
		param.setSessionUser(securityAclService.requireCurrentUser());
		return param;
	}

	private void download(CrFileVO resolved, HttpServletResponse response) throws IOException {
		UserVO actor = securityAclService.requireCurrentUser();
		if (resolved == null || blank(resolved.getDocsFilePathNm())
				|| blank(resolved.getAclObjectType()) || blank(resolved.getAclObjectId())) {
			throw notFound();
		}
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
		access.setObjectType(resolved.getAclObjectType());
		access.setObjectId(resolved.getAclObjectId());
		access.setFileNo(blank(resolved.getAclFileNo()) ? "*" : resolved.getAclFileNo());
		access.setRequestNo(resolved.getRequestNo());
		try {
			securityAclService.requireAccess(access);
		} catch (AccessDeniedException exception) {
			record(actor, resolved, "FAIL", "ACL_DENIED", "CR attachment download was denied.");
			throw exception;
		}
		File file = new File(resolved.getDocsFilePathNm());
		if (!file.isFile()) {
			record(actor, resolved, "FAIL", "FILE_NOT_FOUND", "CR attachment file was not found.");
			throw notFound();
		}
		String downloadName = safeName(resolved.getOrgFileNm(), file.getName());
		String mimeType = URLConnection.guessContentTypeFromName(downloadName);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
			ContentDisposition.inline().filename(downloadName, StandardCharsets.UTF_8).build().toString());
		response.setContentLengthLong(file.length());
		try (InputStream inputStream = new FileInputStream(file)) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			response.getOutputStream().flush();
			record(actor, resolved, "SUCCESS", null, "CR attachment download completed.");
		} catch (IOException exception) {
			record(actor, resolved, "FAIL", "IO_ERROR", "CR attachment download failed.");
			throw exception;
		}
	}

	private void record(UserVO actor, CrFileVO resource, String result, String reason, String message) {
		securityAclService.recordDownloadResult(actor, result, reason, resource.getAclObjectType(),
			resource.getAclObjectId(), resource.getAclFileNo(), resource.getRequestNo(), message);
	}

	private String safeName(String name, String fallback) {
		String value = blank(name) ? fallback : name;
		value = value.replace('\\', '/');
		value = value.substring(value.lastIndexOf('/') + 1).replace("\r", "").replace("\n", "").replace("\"", "");
		return blank(value) ? "download.bin" : value;
	}

	private boolean blank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
	}
}


