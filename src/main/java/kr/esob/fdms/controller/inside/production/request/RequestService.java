package kr.esob.fdms.controller.inside.production.request;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.controller.inside.production.common.AcceptanceInfoVO;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.controller.inside.production.productionstatus.ProductionStatusService;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.ObjectUtil;

@Service
public class RequestService implements CommonService{

	@Inject
	RequestDao dao;

	@Inject
	DateUtil dateUtil;

	@Inject
	ProductionStatusService productionStatusService;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	@Transactional
	public ResultVO distributionRequest(RequestParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		List<RequestMappingParam> mappingList  = new ArrayList<RequestMappingParam>();
		List<DeployInfoVO> deployInfoVoList = new ArrayList<DeployInfoVO>();

		param.setRequestType("PRODUCT");
		param.setRequestPurpose(param.getDeployType());
		param.setApprovalLineId(6);
		param.setObjectType(param.getProductionList().get(0).getObjectType());
		param.setObjectTypeCode(ObjectType.PRODUCT_DOC.getCode());
		for(ProductionInfoVO vo : param.getProductionList()) {
			RequestMappingParam requestMappingParam = new RequestMappingParam();
			BeanUtils.copyProperties(requestMappingParam, vo);
			requestMappingParam.setTotalPageNo(vo.getTotalPageCnt());
			requestMappingParam.setProductCd(vo.getProductNo());
			mappingList.add(requestMappingParam);
			for(AcceptanceInfoVO acceptanceInfoVo : param.getAcceptanceList()) {
				DeployInfoVO deployInfoVo = new DeployInfoVO();
				deployInfoVo.setObjectId(vo.getObjectId());
				deployInfoVo.setObjectNo(vo.getObjectNo());
				deployInfoVo.setDeployDeptCd(acceptanceInfoVo.getDeptCd());
				deployInfoVo.setDeployUserCd(acceptanceInfoVo.getUserCd());
				deployInfoVo.setDeployCount(vo.getDeployCount());
				deployInfoVo.setCopy(acceptanceInfoVo.getCopy());
				deployInfoVo.setDestroyCount(vo.getDestroyCount());
				deployInfoVo.setDestroyStatusCd("DESTROY");
				deployInfoVoList.add(deployInfoVo);
			}
		}
		param.setDeployInfoList(deployInfoVoList);
		resultVo = productionStatusService.selectDisposalRequestObject(param);
		if(!resultVo.isSuccess()) {
			return resultVo;
//			throw new ResultReturnException(resultVo, "결재진행중");
		}
		param.setRequestMappingParamList(mappingList.stream().filter(ObjectUtil.distinctByKey(m -> m.getObjectId())).collect(Collectors.toList()));
		param.setRequestFileParamList(selectFileList(param));
		param.setDeployInfoList(deployInfoVoList);

		distributeRequest(param);

		resultVo.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getTeamLeader());
				mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_APPROVAL);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return resultVo;
	}

	private void distributeRequest(RequestParam param) throws IllegalAccessException, InvocationTargetException {
		dao.insertProductionRequest(param);
		dao.insertProductionRequestDetail(param);
		dao.insertProductionRequestMapping(param);
		dao.insertProductionRequestDeploy(param);
		dao.insertProductionRequestDeployInfo(param);
		dao.insertProductionRequestFile(param);
	}

	private List<RequestFileParam> selectFileList(RequestParam param){
		return dao.selectFileList(param);
	}

	public void setSearchAllParam(RequestListParam param) {
		if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
			Gson gson = new Gson();
			param.setProductionList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<ProductionInfoVO>>() {}.getType()));
		}
	}

	public ResultVO printRequest(RequestParam param) {
		ResultVO result = new ResultVO();
		param.setRequestType("PRODUCT");
		param.setRequestPurpose("PRINT");
//		param.setRequestPurpose(param.getDeployType());
		param.setApprovalLineId(6);
		param.setObjectType(param.getPrintRequestList().get(0).getObjectType());
		param.setObjectTypeCode(ObjectType.PRODUCT_DOC.getCode());

		param.setRequestMappingParamList(dao.selectMappingList(param));

		//fdms_request의사업장
		param.setBusinessAreaCd(param.getRequestMappingParamList().get(0).getBusinessAreaCd());
		//배포 시작일
		param.setDeployDate(dateUtil.getToday("yyyyMMdd"));
		//출력 기한
		param.setValidDate(dateUtil.getAddDay("7", "yyyyMMdd"));

		dao.insertProductionRequest(param);
		dao.insertProductionRequestDetail(param);
		dao.insertProductionRequestMapping(param);

		param.setRequestFileParamList(selectFileList(param));

		dao.insertProductionRequestFile(param);
		//배포받을 사람
		List<AcceptanceInfoVO> acceptanceList = new ArrayList<AcceptanceInfoVO>();
		AcceptanceInfoVO accInfoVo = new AcceptanceInfoVO();
		accInfoVo.setUserCd(param.getRequestUserCd());
		acceptanceList.add(accInfoVo);
		param.setAcceptanceList(acceptanceList);
		dao.insertProductionRequestDeploy(param);
//		dao.insertProductionRequestDeployInfo(param);

		result.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getTeamLeader());
				mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_PRINT_APPROVAL);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public ResultVO selectDeployAcceptCheck(RequestParam param)  throws Exception {
		ResultVO resultVo = new ResultVO();

		if (dao.selectDeployAcceptCheck(param) == 0)
			resultVo.setSuccess(true);
		else
			resultVo.setSuccess(false);

		return resultVo;
	}
}
