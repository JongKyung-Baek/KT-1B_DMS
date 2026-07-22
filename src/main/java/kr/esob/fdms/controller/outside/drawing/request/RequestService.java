package kr.esob.fdms.controller.outside.drawing.request;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Service
public class RequestService implements CommonService {

	@Inject
	RequestDao dao;

	@Inject
	CommonRequestDao requestDao;

	@Inject
	CommonRequestService commonService;

	public List<DrawingInfoVO> selectInspectionInfo(DrawingInfoVO drawingInfoVO) {
		return dao.selectInspectionInfo(drawingInfoVO);
	}

	public ResultVO distributionRequest(RequestParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		List<ObjectInfoVO> normalList  = new ArrayList<ObjectInfoVO>();
		List<ObjectInfoVO> developmentList  = new ArrayList<ObjectInfoVO>();
		List<ObjectInfoVO> productionList  = new ArrayList<ObjectInfoVO>();
		for(ObjectInfoVO vo : param.getList()) {
			if("Y".equals(vo.getProtectYn())) {
				if(Constant.DEVELOPMENT.equals(vo.getBusinessTypeCd())) {
					developmentList.add(vo);
					param.setDevelopmentRequest(true);
				}else if(Constant.PRODUCTION.equals(vo.getBusinessTypeCd())) {
					productionList.add(vo);
					param.setProductionRequest(true);
				}
			}else {
				normalList.add(vo);
				param.setNormalRequest(true);
			}
		}

		if("general".equals(param.getFileDistributionType())) {
			param.setDistributionNormalYn("Y");
			param.setDistributionSpecialYn("N");
		}else if("security".equals(param.getFileDistributionType())) {
			param.setDistributionNormalYn("N");
			param.setDistributionSpecialYn("Y");
		}else {
			param.setDistributionNormalYn("N");
			param.setDistributionSpecialYn("N");
		}
		
		if(param.isNormalRequest()) {
			param.setList(normalList);
			param.setFileList(normalList);
			param.setApprovalLineId(2);
			param.setProtectYn("N");
			commonService.insertRequest(param);
		}
		if(param.isDevelopmentRequest()) {
			param.setList(developmentList);
			param.setFileList(developmentList);
			param.setApprovalLineId(1);
			param.setProtectYn("Y");
			commonService.insertRequest(param);
		}
		if(param.isProductionRequest()) {
			param.setList(productionList);
			param.setFileList(productionList);
			param.setApprovalLineId(1);
			param.setProtectYn("Y");
			commonService.insertRequest(param);
		}
		resultVo.setSuccess(true);
		return resultVo;
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

	public List<DrawingInfoVO> selectRequestStatusList(RequestParam param){
		return dao.selectRequestStatusList(param);
	}

	public List<DrawingInfoVO> selectDrawingDetailList(DrawingInfoVO drawingInfoVO){
		return dao.selectDrawingDetailList(drawingInfoVO);
	}


}
