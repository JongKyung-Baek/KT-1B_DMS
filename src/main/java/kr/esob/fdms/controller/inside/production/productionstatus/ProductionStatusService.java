package kr.esob.fdms.controller.inside.production.productionstatus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.controller.inside.production.common.AcceptanceInfoVO;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.request.RequestDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.util.ObjectUtil;

@Service
public class ProductionStatusService implements CommonService{

	@Inject
	ProductionStatusDao dao;

	@Inject
	RequestDao requestDao;

	@Inject
	DocsMailService mailService;

	@Override
	public List selectList(Object obj) {
		return dao.selectList(obj);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public List<ProductionReplaceRequestVO> selectProductionReplace(ReplaceRequestParam param){
		return dao.selectProductionReplace(param);
	}

	public ResultVO distributionRequest(RequestParam param) throws IllegalAccessException, InvocationTargetException {
		ResultVO resultVo = new ResultVO();
		List<RequestMappingParam> mappingList  = new ArrayList<RequestMappingParam>();
		List<DeployInfoVO> deployInfoVoList = new ArrayList<DeployInfoVO>();
		List<AcceptanceInfoVO> acceptanceList = new ArrayList<AcceptanceInfoVO>();

		param.setRequestType("PRODUCT");
		param.setRequestPurpose(param.getDeployType());
		param.setApprovalLineId(6);
		param.setObjectType(param.getReplaceList().get(0).getObjectType());
		param.setObjectTypeCode(ObjectType.PRODUCT_DOC.getCode());
		for(ProductionReplaceRequestVO vo : param.getReplaceList()) {
			DeployInfoVO deployInfoVo = new DeployInfoVO();
			RequestMappingParam requestMappingParam = new RequestMappingParam();
			AcceptanceInfoVO acceptance = new AcceptanceInfoVO();
			BeanUtils.copyProperties(acceptance, vo);
			BeanUtils.copyProperties(requestMappingParam, vo);
			requestMappingParam.setTotalPageNo(vo.getTotalPageCnt());
			requestMappingParam.setProductCd(vo.getProductNo());
			mappingList.add(requestMappingParam);
			deployInfoVo.setObjectId(vo.getObjectId());
			deployInfoVo.setObjectNo(vo.getObjectNo());
			deployInfoVo.setDeployDeptCd(vo.getDeptCd());
			deployInfoVo.setDeployUserCd(vo.getUserCd());
			deployInfoVo.setDeployCount(vo.getDeployCount());
			deployInfoVo.setCopy(vo.getCopy());
			deployInfoVo.setDestroyCount(vo.getDestroyCount());
			deployInfoVo.setDestroyStatusCd("DESTROY");
			deployInfoVoList.add(deployInfoVo);
			acceptanceList.add(acceptance);
		}
		param.setDeployInfoList(deployInfoVoList);
		resultVo = selectDisposalRequestObject(param);
		if(!resultVo.isSuccess()) {
			return resultVo;
		}
		param.setAcceptanceList(acceptanceList.stream().filter(ObjectUtil.distinctByKey(m -> m.getUserCd())).collect(Collectors.toList()));
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
		requestDao.insertProductionRequest(param);
		requestDao.insertProductionRequestDetail(param);
		requestDao.insertProductionRequestMapping(param);
		requestDao.insertProductionRequestDeploy(param);
		requestDao.insertProductionRequestDeployInfo(param);
		requestDao.insertProductionRequestFile(param);
	}

	private List<RequestFileParam> selectFileList(RequestParam param){
		return requestDao.selectFileList(param);
	}

	public List<DistributionHistoryVO> selectDistributionList(DistributionHistoryVO param){
		List<DistributionHistoryVO> rtnList = dao.selectDeployInfoList(param);
		for(DistributionHistoryVO vo : rtnList) {
			vo.setList(dao.selectHistoryDetailList(vo));
		}
		return rtnList;
	}

	public ResultVO selectDisposalRequestObject(Object param){
		ResultVO resultVo = new ResultVO();
		List<DistributionHistoryVO> list = dao.selectDisposalRequestObject(param);
		resultVo.setSuccess(true);
		if(list.size() > 0) {
			String destroyData = "(";
			resultVo.setSuccess(false);
			resultVo.setFailReason("destroyInProgress");
			for(DistributionHistoryVO vo : list) {
				destroyData += vo.getDeployUserNm() + "/" + vo.getObjectNo() + ", ";
			}
			destroyData = destroyData.substring(0, destroyData.length()-2);
			destroyData += ")";
			resultVo.setData(destroyData);
		}
		return resultVo;
	}


}
