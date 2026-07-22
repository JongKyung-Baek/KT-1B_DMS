package kr.esob.fdms.controller.outside.cr.request;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;
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
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
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
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

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

				String srcUrl = Seed128Cipher.encrypt(SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
				String dstUrl = Seed128Cipher.encrypt(SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
				String srcFilePath = Seed128Cipher.encrypt(filePathNm+fileName, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
				String dstFilePath = Seed128Cipher.encrypt(SystemConfig.getSystemConfigValue("CR_EXCELFILE_PATH") + crParam.getCrNo().substring(2, 6) + "\\", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
				String dstFileNm = Seed128Cipher.encrypt(fileName, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
				
				JSONObject result = FileUtil.callSender(srcUrl, dstUrl, srcFilePath, dstFilePath, dstFileNm);
				
				crParam.setFilePathNm(SystemConfig.getSystemConfigValue("CR_EXCELFILE_PATH") + crParam.getCrNo().substring(2, 6) + "\\" + result.getString("fileNm"));
				crParam.setFileNm(result.getString("fileNm"));
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
		return dao.getCrRequestInfo(param);
	}

	public CrInfoVO selectAcceptanceInfo(CrRequestParam param) {
		CrInfoVO vo = new CrInfoVO();
		CrParam crParam = new CrParam();
		crParam.setCrNo(param.getCrNo());
		vo.setFileList(commonCrDao.selectOutsideFileList(crParam));
		return vo;
	}

	public ResultVO approve(OutsideCrParam param) {
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.APPROVAL);
		param.setReqStatusCd(Constant.REQUEST);
		param.setApprovalStatusCd(Constant.WAITING);
		param.setStatusCd(CrStatusCdInfo.VENDOR_APPROVAL);
		param.setRejectDesc(null);
		dao.updateRequest(param);
		dao.updateRequestDetail(param);
		dao.updateCr(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO approvalReject(OutsideCrParam param) {
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.REJECT);
		param.setReqStatusCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.WAITING);
		param.setStatusCd(CrStatusCdInfo.VENDOR_REJECT);
		dao.updateRequest(param);
		dao.updateRequestDetail(param);
		dao.updateCr(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

}
