package kr.esob.fdms.controller.outside.commondestroystatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class CommonDestroyStatusService {

	@Inject
	CommonDestroyStatusDao dao;

	@Inject
	SecurityAclService securityAclService;

	public DestroyStatusInfoVO selectDestroyStatus(DestroyStatusParam param) {
		UserVO actor = securityAclService.requireCurrentUser();
		if (param == null || isBlank(param.getLastDestroyRequestNo())) {
			throw notFound();
		}
		param.setSessionUser(actor);
		DestroyStatusInfoVO vo = dao.selectDestroyStatus(param);
		if (vo == null) {
			throw notFound();
		}
		vo.setFileList(dao.selectDestroyFileList(param));
		return vo;
	}

	public void destroyFileDown(DestroyFileDownloadParam param, HttpServletResponse response) throws IOException {
		UserVO actor = securityAclService.requireCurrentUser();
		if (param == null || isBlank(param.getDestroyRequestNo()) || param.getDestroyFileSeq() <= 0) {
			throw notFound();
		}
		param.setSessionUser(actor);

		List<DestroyFileVO> targets = dao.selectAuthorizedDownloadTargets(param);
		if (targets == null || targets.isEmpty()) {
			throw notFound();
		}

		for (DestroyFileVO target : targets) {
			FileAccessRequest access = toAccessRequest(target);
			try {
				securityAclService.requireAccess(access);
			} catch (AccessDeniedException exception) {
				securityAclService.recordDownloadResult(actor, "FAIL", "ACL_DENIED",
					target.getObjectType(), target.getObjectId(), target.getFileNo(),
					target.getRequestNo(), "Destruction evidence download was denied.");
				throw exception;
			}
		}

		DestroyFileVO resolved = targets.get(0);
		File file = isBlank(resolved.getFilePath()) ? null : new File(resolved.getFilePath());
		if (file == null || !file.isFile()) {
			recordResult(targets, actor, "FAIL", "FILE_NOT_FOUND",
				"Destruction evidence file was not found.");
			throw notFound();
		}

		String downloadName = safeFileName(resolved.getFileName(), file.getName());
		String mimeType = URLConnection.guessContentTypeFromName(downloadName);
		if (isBlank(mimeType)) {
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
			recordResult(targets, actor, "SUCCESS", null,
				"Destruction evidence file download completed.");
		} catch (IOException exception) {
			recordResult(targets, actor, "FAIL", "IO_ERROR",
				"Destruction evidence file download failed.");
			throw exception;
		}
	}

	private FileAccessRequest toAccessRequest(DestroyFileVO target) {
		if (target == null || isBlank(target.getObjectType()) || isBlank(target.getObjectId())) {
			throw notFound();
		}
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
		access.setObjectType(target.getObjectType());
		access.setObjectId(target.getObjectId());
		access.setFileNo(isBlank(target.getFileNo()) ? "*" : target.getFileNo());
		access.setRequestNo(target.getRequestNo());
		return access;
	}

	private void recordResult(List<DestroyFileVO> targets, UserVO actor, String resultCd,
							  String reasonCd, String message) {
		for (DestroyFileVO target : targets) {
			securityAclService.recordDownloadResult(actor, resultCd, reasonCd,
				target.getObjectType(), target.getObjectId(), target.getFileNo(),
				target.getRequestNo(), message);
		}
	}

	private String safeFileName(String requestedName, String fallback) {
		String value = isBlank(requestedName) ? fallback : requestedName;
		value = value.replace('\\', '/');
		int slash = value.lastIndexOf('/');
		if (slash >= 0) {
			value = value.substring(slash + 1);
		}
		value = value.replace("\r", "").replace("\n", "").replace("\"", "");
		return isBlank(value) ? "download.bin" : value;
	}

	private ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
