package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.server.ResponseStatusException;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileDownloadParam;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class DisposalAcceptanceService implements CommonService{

	@Inject
	DisposalAcceptanceDao dao;

	@Inject
	DateUtil dateUtil;

	@Inject
	SecurityAclService securityAclService;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public DisposalAcceptancePopupVO selectDisposalInfo(DisposalAcceptanceParam param) {
		param.setSessionUser(securityAclService.requireCurrentUser());
		DisposalAcceptancePopupVO info = dao.selectDisposalInfo(param);
		if (info == null) {
			throw notFound();
		}
		return info;
	}

	public List<DisposalAcceptancePopupListVO> selectPopupList(DisposalAcceptanceParam param){
		return dao.selectPopupList(param);
	}

	public List<DestroyFileVO> selectDisposalFileList(DisposalAcceptanceParam param){
		return dao.selectDisposalFileList(param);
	}

	public void fileDownload(DestroyFileDownloadParam param, HttpServletResponse response) throws IOException {
		UserVO actor = securityAclService.requireCurrentUser();
		if (param == null || blank(param.getDestroyRequestNo()) || param.getDestroyFileSeq() <= 0) {
			throw notFound();
		}
		param.setSessionUser(actor);
		List<DestroyFileVO> targets = dao.selectAuthorizedDownloadTargets(param);
		if (targets == null || targets.isEmpty()) {
			throw notFound();
		}
		for (DestroyFileVO target : targets) {
			FileAccessRequest access = access(target);
			try {
				securityAclService.requireAccess(access);
			} catch (AccessDeniedException exception) {
				securityAclService.recordDownloadResult(actor, "FAIL", "ACL_DENIED",
					target.getObjectType(), target.getObjectId(), target.getFileNo(),
					target.getRequestNo(), "Disposal evidence download was denied.");
				throw exception;
			}
		}
		DestroyFileVO resolved = targets.get(0);
		File file = blank(resolved.getFilePath()) ? null : new File(resolved.getFilePath());
		if (file == null || !file.isFile()) {
			record(targets, actor, "FAIL", "FILE_NOT_FOUND");
			throw notFound();
		}
		String name = safeName(resolved.getFileName(), file.getName());
		response.setContentType("application/octet-stream");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
			ContentDisposition.inline().filename(name, StandardCharsets.UTF_8).build().toString());
		response.setContentLengthLong(file.length());
		try (InputStream inputStream = new FileInputStream(file)) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			response.getOutputStream().flush();
			record(targets, actor, "SUCCESS", null);
		} catch (IOException exception) {
			record(targets, actor, "FAIL", "IO_ERROR");
			throw exception;
		}
	}

	private FileAccessRequest access(DestroyFileVO target) {
		if (target == null || blank(target.getObjectType()) || blank(target.getObjectId())) {
			throw notFound();
		}
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
		access.setObjectType(target.getObjectType());
		access.setObjectId(target.getObjectId());
		access.setFileNo(blank(target.getFileNo()) ? "*" : target.getFileNo());
		access.setRequestNo(target.getRequestNo());
		return access;
	}

	private void record(List<DestroyFileVO> targets, UserVO actor, String result, String reason) {
		for (DestroyFileVO target : targets) {
			securityAclService.recordDownloadResult(actor, result, reason, target.getObjectType(),
				target.getObjectId(), target.getFileNo(), target.getRequestNo(),
				"Disposal evidence download " + ("SUCCESS".equals(result) ? "completed." : "failed."));
		}
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

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveApproval(DisposalAcceptanceParam param) {
		validateApprovalRequest(param);
		UserVO actor = securityAclService.requireCurrentUser();
		param.setSessionUser(actor);
		if (dao.selectApprovalTargetForUpdate(param) == null) {
			throw new AccessDeniedException("Disposal approval request is not accessible");
		}

		ResultVO resultVo = new ResultVO();
		requireAffectedRows(dao.updateRequestFile(param), "update disposal request files");
		requireAffectedRows(dao.updateApprovalFile(param), "update disposal approval files");
		requireSingleRow(dao.updateDestroyRequest(param), "complete disposal request");
		requireSingleRow(dao.updateDestroyRequestDetail(param), "complete disposal approval step");
		resultVo.setSuccess(true);
		return resultVo;
	}

	private void validateApprovalRequest(DisposalAcceptanceParam param) {
		if (param == null || blank(param.getDestroyRequestNo())
				|| (!"A".equals(param.getSaveFlag()) && !"R".equals(param.getSaveFlag()))) {
			throw new IllegalArgumentException("Invalid disposal approval request");
		}
		param.setDestroyRequestNo(param.getDestroyRequestNo().trim());
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private void requireAffectedRows(int affectedRows, String operation) {
		if (affectedRows < 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

}
