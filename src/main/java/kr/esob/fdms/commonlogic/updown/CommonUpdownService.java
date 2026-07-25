package kr.esob.fdms.commonlogic.updown;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@Service
public class CommonUpdownService implements CommonService {

	@Inject
	CommonUpdownDao dao;

	@Inject
	SecurityAclService securityAclService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@Transactional(rollbackFor = Exception.class)
	public List selectList(Object obj) {
		if (!(obj instanceof CommonUpdownParam)) {
			throw new IllegalArgumentException("Download request is required.");
		}
		CommonUpdownParam param = (CommonUpdownParam) obj;
		UserVO actor = securityAclService.requireCurrentUser();
		param.setSessionUser(actor);
		if (param.getList() == null || param.getList().isEmpty()) {
			throw new IllegalArgumentException("At least one download resource is required.");
		}

		List<CommonUpdownFileVO> authorizedResources = resolveAuthorizedResources(param, actor);
		List<CommonUpdownFileVO> response = new ArrayList<CommonUpdownFileVO>();
		String userName = actor.getUsername();

		for (CommonUpdownFileVO resource : authorizedResources) {
			if ("DISTRIBUTION".equals(param.getReqType())
					&& isRestBackedObjectType(resource.getObjectType())) {
				// This endpoint only prepares REST-backed metadata. The V2 completion
				// flow persists SUCCESS/FAIL after the actual bytes are delivered.
				securityAclService.recordDownloadResult(actor, "PREPARED", null,
						resource.getObjectType(), resource.getObjectId(), resource.getFileNo(),
						resource.getRequestNo(), "REST-backed download metadata prepared.");
				response.add(resource);
				continue;
			}

			prepareLegacyDownload(param, actor, resource, userName);
			// All counters, history and ACL audit rows must be durable before a
			// downloadable item becomes visible in the response.
			response.add(resource);
		}
		return response;
	}

	private List<CommonUpdownFileVO> resolveAuthorizedResources(CommonUpdownParam param, UserVO actor) {
		List<CommonUpdownFileVO> resources = new ArrayList<CommonUpdownFileVO>();
		for (CommonUpdownParam item : param.getList()) {
			if (item == null) {
				throw new IllegalArgumentException("Download resource metadata is required.");
			}
			String requestNo = trim(item.getRequestNo());
			String objectId = trim(isBlank(item.getObjectId()) ? item.getDocSeq() : item.getObjectId());
			String fileNo = trim(isBlank(item.getFileNo()) ? item.getFileSeq() : item.getFileNo());
			if (isBlank(requestNo) || isBlank(objectId) || isBlank(fileNo)) {
				throw new IllegalArgumentException("requestNo, objectId and fileNo are required.");
			}

			CommonUpdownFileVO resource = dao.selectDownloadResource(requestNo, objectId, fileNo);
			if (resource == null) {
				throw new IllegalArgumentException("The requested download resource was not found.");
			}
			resource.setObjectType(normalizeDbObjectType(resource.getObjectType()));
			if (dao.countDownloadBusinessAccess(
					resource.getRequestNo(), resource.getObjectId(), resource.getFileNo(), actor.getUserCd()) == 0) {
				throw new AccessDeniedException("The current user is not a requester or deployment target.");
			}

			FileAccessRequest access = new FileAccessRequest();
			access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
			access.setObjectType(resource.getObjectType());
			access.setObjectId(resource.getObjectId());
			access.setFileNo(resource.getFileNo());
			access.setRequestNo(resource.getRequestNo());
			securityAclService.requireAccess(access);
			resources.add(resource);
		}
		return resources;
	}

	private void prepareLegacyDownload(CommonUpdownParam param, UserVO actor,
			CommonUpdownFileVO resource, String userName) {
		String srcUrl = SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE");
		String dstUrl = SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE");
		String sourceFilePath;
		String targetFileName;
		String targetFilePath = SystemConfig.getSystemConfigValue("UPDOWN_PATH");
		if ("DISTRIBUTION".equals(param.getReqType())) {
			sourceFilePath = resource.getFilePathNm();
			targetFileName = targetFilePath + resource.getOrgFileNm();
		} else if ("UNREG".equals(param.getReqType())) {
			sourceFilePath = resource.getFilePathNm();
			targetFileName = resource.getFileNm();
		} else {
			throw new IllegalArgumentException("Unsupported legacy download request type.");
		}
		requireConfigured(srcUrl, "SERVER_URL_INSIDE");
		requireConfigured(dstUrl, "SERVER_URL_OUTSIDE");
		requireConfigured(sourceFilePath, "source file path");
		requireConfigured(targetFilePath, "UPDOWN_PATH");
		requireConfigured(targetFileName, "target file name");

		Integer currentDownloadCount = dao.getDownloadCount(
				resource.getRequestNo(), resource.getObjectId(), resource.getFileNo());
		if (currentDownloadCount == null) {
			throw new IllegalStateException("Download counter target was not found.");
		}
		if (currentDownloadCount.intValue() >= 3) {
			throw new IllegalStateException("Download count limit has been reached.");
		}

		try {
			JSONObject transfer = FileUtil.callSender(
					Seed128Cipher.encrypt(srcUrl, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
					Seed128Cipher.encrypt(dstUrl, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
					Seed128Cipher.encrypt(sourceFilePath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
					Seed128Cipher.encrypt(targetFilePath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
					Seed128Cipher.encrypt(targetFileName, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING));
			String downloadedName = requireSuccessfulTransfer(transfer);

			if (dao.plusDownloadCount(
					resource.getRequestNo(), resource.getObjectId(), resource.getFileNo()) != 1) {
				throw new IllegalStateException("Download counter update failed.");
			}
			Integer updatedDownloadCount = dao.getDownloadCount(
					resource.getRequestNo(), resource.getObjectId(), resource.getFileNo());
			if (updatedDownloadCount == null) {
				throw new IllegalStateException("Updated download counter was not found.");
			}
			Map<String, Object> objectInfo =
					dao.getObjectNoObjectNm(resource.getRequestNo(), resource.getObjectId());
			if (objectInfo == null) {
				throw new IllegalStateException("Download history subject was not found.");
			}
			if (dao.addToDownHistory(
					resource.getRequestNo(), resource.getObjectId(), updatedDownloadCount,
					(String) objectInfo.get("objectNo"), (String) objectInfo.get("objectNm"),
					userName, new java.util.Date(), downloadedName) != 1) {
				throw new IllegalStateException("Download history insert failed.");
			}
			securityAclService.recordDownloadResult(actor, "SUCCESS", null,
					resource.getObjectType(), resource.getObjectId(), resource.getFileNo(),
					resource.getRequestNo(), "Legacy download file prepared.");

			resource.setFilePathNm(targetFilePath);
			resource.setFileNm(downloadedName);
		} catch (Constant.LegacyCryptoConfigurationException e) {
			log.error("Legacy download encryption is unavailable; configure KT1B_LEGACY_CRYPTO_KEY");
			throw e;
		} catch (RuntimeException e) {
			log.warn("Failed to prepare a legacy download file. cause={}",
					e.getClass().getSimpleName());
			throw e;
		} catch (Exception e) {
			log.warn("Failed to prepare a legacy download file. cause={}",
					e.getClass().getSimpleName());
			throw new IllegalStateException("Legacy download file preparation failed.", e);
		}
	}

	static String requireSuccessfulTransfer(JSONObject transfer) {
		return FileUtil.requireSuccessfulTransferFileName(transfer);
	}

	private void requireConfigured(String value, String name) {
		if (isBlank(value)) {
			throw new IllegalStateException(name + " is not configured.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private String normalizeDbObjectType(String objectType) {
		String normalized = trim(objectType).toUpperCase(Locale.ROOT);
		if ("PRODUCT_DOC".equals(normalized)) {
			normalized = "PRODUCT_DOCUMENT";
		} else if ("PEERREVIEW".equals(normalized)) {
			normalized = "PEER_REVIEW";
		}
		return securityAclService.normalizeObjectType(normalized);
	}

	private boolean isRestBackedObjectType(String objectType) {
		return "DOCUMENT".equals(objectType)
				|| "DRAWING".equals(objectType)
				|| "SW".equals(objectType)
				|| "PRODUCT_SW".equals(objectType);
	}

	@Override
	public int selectListCount(Object obj) {
		return 0;
	}

	public Map<String, Object> getUploadConfig() {
		return dao.getUploadConfig();
	}

	public boolean copyFile(CommonUpdownFileVO vo) {
		return false;
	}
}
