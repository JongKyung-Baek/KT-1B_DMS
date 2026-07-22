package kr.esob.fdms.controller.outside.commonrequest;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommonRequestService {

	@Inject
	CommonRequestDao dao;

	@Inject
	Prop prop;

	@Inject
	AuthorizationDao authorizationDao;
	
	@Inject
	DocsMailService mailService;

	public void insertRequest(RequestParam param) {
		dao.insertDocsRequest(param);
		dao.insertDocsRequestMapping(param);
		dao.insertDocsRequestDetail(param);
		dao.insertDocsRequestDeploy(param);
		if("DISTRIBUTION".equals(param.getRequestType())) {
			dao.insertDocsRequestFile(param);
		}
		try {
			sendMail(param);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMail(RequestParam param) {
		MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getAcceptanceUserCd());
		mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_APPROVAL_OUTSIDE);
		mailService.sendDocsMail(mailInfoVo);
	}
	
	public UserVO selectUserInfo(UserVO userVo) {
		return dao.selectUserInfo(userVo);
	}

	public UserVO selectUserEmail(UserVO userVo) {
		return dao.selectUserEmail(userVo);
	}

	public UserVO selectUserInfoById(UserVO userVo) {
		return dao.selectUserInfoById(userVo);
	}

	public CommonRequestStatusVO selectRequestStatus(RequestParam param) {
		return dao.selectRequestStatus(param);
	}

	public List<RequestFileParam> selectFileList(RequestParam param){
		return dao.selectFileList(param);
	}

	public List<ObjectInfoVO> selectSimilarInspectionInfo(ObjectInfoVO param) {
		List<ObjectInfoVO> inspectionList = new ArrayList<ObjectInfoVO>();
		if(ObjectType.DRAWING.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectSimilarDrawingInspectionInfo(param);
		}else if(ObjectType.DOC.getObjectType().equals(param.getObjectType())) {
			inspectionList = dao.selectSimilarDocInspectionInfo(param);
		}else if(ObjectType.SW.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectSimilarSwInspectionInfo(param);
		}else if(ObjectType.PRODUCT_DOC.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectSimilarProductDocInspectionInfo(param);
		}else if(ObjectType.PRODUCT_SW.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectSimilarProductSwInspectionInfo(param);
		}
		return setResult(param, inspectionList);
	}
	public List<ObjectInfoVO> selectInspectionInfo(ObjectInfoVO param) {
		List<ObjectInfoVO> inspectionList = new ArrayList<ObjectInfoVO>();
		if(ObjectType.DRAWING.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectDrawingInspectionInfo(param);
		}else if(ObjectType.DOC.getObjectType().equals(param.getObjectType())) {
			inspectionList = dao.selectDocInspectionInfo(param);
		}else if(ObjectType.SW.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectSwInspectionInfo(param);
		}else if(ObjectType.PRODUCT_DOC.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectProductDocInspectionInfo(param);
		}else if(ObjectType.PRODUCT_SW.getObjectType().equals(param.getObjectType())) {
			inspectionList =  dao.selectProductSwInspectionInfo(param);
		}
		return setResult(param, inspectionList);
	}

	private List<ObjectInfoVO> setResult(ObjectInfoVO param, List<ObjectInfoVO> inspectionList){
		List<ObjectInfoVO> rtnList = param.getList();
		int size = rtnList.size();
		for(int i=size-1; i>=0; i--) {
			for(ObjectInfoVO inspectionInfo : inspectionList) {
				if(rtnList.get(i).getObjectNo().equals(inspectionInfo.getObjectNo())) {
					rtnList.remove(rtnList.get(i));
					break;
				}
			}
		}
		rtnList.addAll(inspectionList);
		for(ObjectInfoVO vo : rtnList) {
			if("N".equals(vo.getInspectionYn().name())) {
				vo.setObjectNm(prop.msg("msg.dataNotFound"));
			}
		}
		return rtnList;
	}
	
	public ResultVO updateVendorAccept(RequestParam param) {
		ResultVO resultVo = new ResultVO();
		dao.updateVendorAccept(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

}
