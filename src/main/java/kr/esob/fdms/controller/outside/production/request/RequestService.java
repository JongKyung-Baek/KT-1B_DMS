package kr.esob.fdms.controller.outside.production.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.util.ObjectUtil;

@Service
public class RequestService implements CommonService {

	@Inject
	RequestDao dao;

	@Inject
	CommonRequestService commonService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public List<ProductionInfoVO> selectDocumentInspectionInfo(ProductionInfoVO productionInfoVo) {
		return dao.selectDocumentInspectionInfo(productionInfoVo);
	}

	public List<ProductionInfoVO> selectSwInspectionInfo(ProductionInfoVO productionInfoVo) {
		return dao.selectSwInspectionInfo(productionInfoVo);
	}



	public ResultVO distributionRequest(RequestParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		List<ObjectInfoVO> list = param.getList();
		param.setList(list.stream().filter(ObjectUtil.distinctByKey(m -> m.getObjectId())).collect(Collectors.toList()));
		param.setFileList(list);
		param.setApprovalLineId(2);
		param.setProtectYn("N");
		commonService.insertRequest(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public List<ObjectInfoVO> selectDocumentRequestStatusList(RequestParam param){
		return dao.selectDocumentRequestStatusList(param);
	}

	public List<ObjectInfoVO> selectSwRequestStatusList(RequestParam param){
		return dao.selectSwRequestStatusList(param);
	}

	public List<ProductionInfoVO> selectProductionDetailList(ProductionInfoVO productionInfoVo){
		return dao.selectProductionDetailList(productionInfoVo);
	}

}
