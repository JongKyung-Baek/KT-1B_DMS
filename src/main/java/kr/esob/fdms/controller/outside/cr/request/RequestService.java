package kr.esob.fdms.controller.outside.cr.request;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.ObjectUtil;
import net.sf.json.JSONObject;

@Service
public class RequestService implements CommonService {

	@Inject
	RequestDao dao;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	CommonRequestDao commonRequestDao;

	@Inject
	SessionValue sessionValue;

	@Inject
	AuthorizationService authorizationService;

	@Inject
	CommonCrDao commonCrDao;

	public List<DrawingInfoVO> getDrawingInfo(DrawingInfoVO drawingInfoVo) {
		return dao.getDrawingInfo(drawingInfoVo);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		RequestListParam listParam = requireListParam(param);
		listParam.setSessionUser(requireAuthenticatedActor());
		return dao.selectList(listParam);
	}

	@Override
	public int selectListCount(Object param) {
		RequestListParam listParam = requireListParam(param);
		listParam.setSessionUser(requireAuthenticatedActor());
		return dao.selectListCount(listParam);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO insertCrRequest(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		//도면정보, CR정보, 결재 요청정보에 관련된 파라미터 정리
//		DrawingInfoVO infoVo = (DrawingInfoVO) JSONObject.toBean(JSONObject.fromObject(request.getParameter("drawingInfo")), DrawingInfoVO.class);
		CrRequestParam crParam = (CrRequestParam) ObjectUtil.jsonToObj(request.getParameter("crParam"), CrRequestParam.class);
		RequestParam param = new RequestParam();
		ObjectInfoVO mappingParam = new ObjectInfoVO();
		setParam(crParam, param, mappingParam);
		//결재요청
		commonRequestDao.insertDocsRequest(param);
		commonRequestDao.insertDocsRequestDeploy(param);
		commonRequestDao.insertDocsRequestDetail(param);
		commonRequestDao.insertDocsRequestMapping(param);

		crParam.setCrNo(param.getList().get(0).getObjectId());
		//ITN_CR insert
		dao.insertCrInfo(crParam);

		//CR첨부파일 저장
		Iterator<String> itr = request.getFileNames();
		String filePathNm = SystemConfig.getSystemConfigValue("CR_PATH_OUTSIDE") + crParam.getCrNo().replace("-", File.separator) + File.separator;
		File filePath = new File(filePathNm);
		if(!filePath.exists())filePath.mkdirs();
		if(itr.hasNext()) {
			List<MultipartFile> list = request.getFiles(itr.next().toString());
			crParam.setFileList(list);
			for(int i=0; i<list.size(); i++) {
				String fileName = UUID.randomUUID().toString().replace("-", "");
				String originalFilename = list.get(i).getOriginalFilename();

				crParam.setRequestNo(param.getRequestNo());
				crParam.setObjectId(param.getList().get(0).getObjectId());
				crParam.setFilePathNm(filePathNm+fileName);
				crParam.setOrgFileNm(originalFilename.substring(originalFilename.lastIndexOf("\\")+1, originalFilename.length()));
				crParam.setFileNm(fileName);
				crParam.setFileSize(list.get(i).getSize());
				crParam.setFileNo(i+1);

				File file = new File(filePathNm + fileName);
				list.get(i).transferTo(file);
				dao.insertRequestFile(crParam);

				String srcUrl = FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE"));
				String dstUrl = FileUtil.encryptTransferArgument(SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE"));
				String srcFilePath = FileUtil.encryptTransferArgument(filePathNm + fileName);
				String targetDirectory = SystemConfig.getSystemConfigValue("CR_EXCELFILE_PATH")
						+ crParam.getCrNo().substring(2, 6) + "\\";
				String dstFilePath = FileUtil.encryptTransferArgument(targetDirectory);
				String dstFileNm = FileUtil.encryptTransferArgument(fileName);
				
				JSONObject result = FileUtil.callSender(srcUrl, dstUrl, srcFilePath, dstFilePath, dstFileNm);
				String transferredFileName = FileUtil.requireSuccessfulTransferFileName(result);
				crParam.setFilePathNm(targetDirectory + transferredFileName);
				crParam.setFileNm(transferredFileName);
				dao.insertCrFile(crParam);
			}
		}


		resultVo.setSuccess(true);
		return resultVo;
	}

	private void setParam(CrRequestParam crParam, RequestParam param, ObjectInfoVO mappingParam) throws IllegalAccessException, InvocationTargetException {
		crParam.setStatusCd(CrStatusCdInfo.REQUEST);
		crParam.setPurchaserTlUid(authorizationService.selectPurchaserTeamLeader(crParam.getPurchaserUid()).getUserId());

		param.setOutApprovalTeamLeaderCd(crParam.getApprovalUser());
		param.setRequestType("CR");
		param.setApprovalLineId(7);
		param.setObjectType(ObjectType.CR.getObjectType());
		param.setObjectTypeCode(ObjectType.CR.getCode());
		param.setBusinessAreaCd(crParam.getBusinessAreaCd());

		UserVO userVo = new UserVO();
		userVo.setUserId(crParam.getVendorUid());
		param.setDeployUserCd(commonRequestService.selectUserInfoById(userVo).getUserCd());
		param.setDeployUserEmail(crParam.getVendorEmailNm());

		userVo.setUserId(crParam.getPurchaserUid());
		param.setAcceptanceUserCd(commonRequestService.selectUserInfoById(userVo).getUserCd());

		List<ObjectInfoVO> normalList = new ArrayList<ObjectInfoVO>();
		BeanUtils.copyProperties(mappingParam, crParam);

		mappingParam.setObjectId(dao.selectCrNo());
		mappingParam.setObjectNo(crParam.getDrawingNo());
		mappingParam.setObjectNm(crParam.getDrawingNm());
		normalList.add(mappingParam);
		param.setList(normalList);
	}

	public RequestStatusPopupVO getCrRequestInfo(CrRequestParam param) {
		if (param == null || isBlank(param.getCrNo())) {
			throw new IllegalArgumentException("CR number is required");
		}
		param.setCrNo(param.getCrNo().trim());
		param.setSessionUser(requireAuthenticatedActor());
		RequestStatusPopupVO info = dao.getCrRequestInfo(param);
		if (info == null) {
			throw new AccessDeniedException("CR request is not accessible");
		}
		return info;
	}

	public CrInfoVO selectAcceptanceInfo(CrRequestParam param) {
		if (param == null || isBlank(param.getCrNo())) {
			throw new IllegalArgumentException("CR number is required");
		}
		UserVO actor = requireAuthenticatedActor();
		param.setCrNo(param.getCrNo().trim());
		param.setSessionUser(actor);
		CrInfoVO vo = new CrInfoVO();
		CrParam crParam = new CrParam();
		crParam.setCrNo(param.getCrNo());
		crParam.setSessionUser(actor);
		vo.setFileList(commonCrDao.selectOutsideFileList(crParam));
		return vo;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO approve(OutsideCrParam param) {
		authorizeApprovalTarget(param);
		param.setActionCd(Constant.APPROVAL);
		param.setReqStatusCd(Constant.REQUEST);
		param.setApprovalStatusCd(Constant.WAITING);
		param.setCurrentProcessSeqNo(3);
		param.setStatusCd(CrStatusCdInfo.VENDOR_APPROVAL);
		param.setRejectDesc(null);
		requireSingleRow(dao.updateRequestDetail(param), "complete vendor CR approval step");
		requireSingleRow(dao.updateRequest(param), "advance vendor-approved CR request");
		requireSingleRow(dao.updateCr(param), "update vendor-approved CR");

		ResultVO resultVo = new ResultVO();
		resultVo.setSuccess(true);
		return resultVo;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO approvalReject(OutsideCrParam param) {
		authorizeApprovalTarget(param);
		param.setActionCd(Constant.REJECT);
		param.setReqStatusCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.WAITING);
		param.setCurrentProcessSeqNo(2);
		param.setStatusCd(CrStatusCdInfo.VENDOR_REJECT);
		requireSingleRow(dao.updateRequestDetail(param), "complete vendor CR rejection step");
		requireSingleRow(dao.updateRequest(param), "reject vendor CR request");
		requireSingleRow(dao.updateCr(param), "update vendor-rejected CR");

		ResultVO resultVo = new ResultVO();
		resultVo.setSuccess(true);
		return resultVo;
	}

	private RequestListParam requireListParam(Object param) {
		if (!(param instanceof RequestListParam)) {
			throw new IllegalArgumentException("Invalid CR request list");
		}
		return (RequestListParam) param;
	}

	private void authorizeApprovalTarget(OutsideCrParam param) {
		if (param == null || isBlank(param.getCrNo())) {
			throw new IllegalArgumentException("CR number is required");
		}

		param.setCrNo(param.getCrNo().trim());
		param.setRequestNo(null);
		param.setFilePathNmList(null);
		param.setActionCd(null);
		param.setApprovalGradeCd(null);
		param.setApprovalStatusCd(null);
		param.setReqStatusCd(null);
		param.setRequestDesc(null);
		param.setCurrentProcessSeqNo(0);
		param.setStatusCd(0);
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);
		param.setActualUserCd(actor.getUserCd());

		String requestNo = dao.selectApprovalTargetForUpdate(param);
		if (isBlank(requestNo)) {
			throw new AccessDeniedException("CR approval request is not accessible");
		}
		param.setRequestNo(requestNo);
	}

	private UserVO requireAuthenticatedActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
			throw new AccessDeniedException("Authenticated user is required");
		}
		return (UserVO) authentication.getPrincipal();
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
