package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistoryService implements CommonService{

	@Inject
    HistoryDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}


	public ResultVO destroyRequest(DestroyRequestParam param) {
		List<Map<String, Object>> arrNonProtect = new ArrayList<Map<String, Object>>();
//		List<Map<String, Object>> arrProtect = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> arrDevelopmentProtect = new ArrayList<Map<String, Object>>();			//개발
		List<Map<String, Object>> arrProductionProtect = new ArrayList<Map<String, Object>>();			//양산
		ResultVO result = new ResultVO();
		for(DestroyRequestParam tempParam : param.getList()) {
		/*
			if("Y".equals(tempParam.getProtectYn())) {			//방산
				//개발, 양산 구분
				if( (null != param.getProtectUserUid()) ) {
				List<Map<String, Object>> protectUserList = dao.getProtectUser(param);
					for(Map<String, Object> tempMap : protectUserList) {
						if("Production".equals(tempMap.get("BUSINESS_TYPE_CD").toString())) {
							param.setProductProtectUid(tempMap.get("USER_CD").toString());
						}else if("Development".equals(tempMap.get("BUSINESS_TYPE_CD").toString())) {
							param.setDevelopmentProtectUserUid(tempMap.get("USER_CD").toString());
						}
					}
				}
				if("Development".equals(tempParam.getBusinessTypeCd())) {		//개발
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("objectId", tempParam.getObjectId());
					tempMap.put("requestNo", tempParam.getRequestNo());
					arrDevelopmentProtect.add(tempMap);
				}else if("Production".equals(tempParam.getBusinessTypeCd())) {	//양산
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("objectId", tempParam.getObjectId());
					tempMap.put("requestNo", tempParam.getRequestNo());
					arrProductionProtect.add(tempMap);
				}
//				Map<String, Object> tempMap = new HashMap<String, Object>();
//				tempMap.put("objectId", tempParam.getObjectId());
//				arrProtect.add(tempMap);
			}else {	// 방산 아님
				*/
//			}else if("N".equals(tempParam.getProtectYn())) {	// 방산 아님
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("requestNo", tempParam.getRequestNo());
				tempMap.put("objectId", tempParam.getObjectId());
				arrNonProtect.add(tempMap);
//			}
		}

		String destoryNo = "";

//		if(arrProtect.size()>0) {	//방산 요청이 있을경우
//			param.setProtectYn("Y");
//			param.setCurrentProcessSeqNo("2");
//			param.setDestroyRequestNo(destoryNo);
//			param.setApprovalLineId("4");
////			INSERT DOCS_DESTROY_REQUEST
//			destoryNo = dao.inserDestoryRequest(param);
//
//			insertRequestInfo(param, arrProtect);
//		}
		
		
		if(arrDevelopmentProtect.size()>0) {	//방산 요청이 있을경우 (개발)
			param.setProtectYn("Y");
			param.setCurrentProcessSeqNo("2");
			param.setDestroyRequestNo(destoryNo);
			param.setApprovalLineId("4");
			param.setBusinessTypeCd("Development");
//			INSERT DOCS_DESTROY_REQUEST
			destoryNo = dao.inserDestoryRequest(param);
			param.setDestroyRequestNo(destoryNo);
			
			insertRequestInfo(param, arrProductionProtect);
		}
		
		if(arrProductionProtect.size()>0) {	//방산 요청이 있을경우 (양산)
			param.setProtectYn("Y");
			param.setCurrentProcessSeqNo("2");
			param.setDestroyRequestNo(destoryNo);
			param.setApprovalLineId("4");
			param.setBusinessTypeCd("Production");
//			INSERT DOCS_DESTROY_REQUEST
			destoryNo = dao.inserDestoryRequest(param);
			param.setDestroyRequestNo(destoryNo);
			insertRequestInfo(param, arrProductionProtect);
		}

		if(arrNonProtect.size()>0) {	//요청이 있을경우 (방산x)
			param.setProtectYn("N");
			param.setCurrentProcessSeqNo("2");
			param.setDestroyRequestNo(destoryNo);
			param.setApprovalLineId("3");
//			INSERT DOCS_DESTROY_REQUEST
			destoryNo = dao.inserDestoryRequest(param);
			param.setDestroyRequestNo(destoryNo);
			insertRequestInfo(param, arrNonProtect);
		}
		result.setSuccess(true);
		return result;
		}


	private void insertRequestInfo(DestroyRequestParam param, List<Map<String, Object>> arrList) {
		List<ApprovalLineDetailVO> appLindDetail = dao.getDocsApprovalLineDetail(param.getApprovalLineId());
		//INSERT DOCS_REQUEST_DETAIL
		for(int i=1; i<=appLindDetail.size(); i++) {
			param.setProcessSeq(Integer.toString(i));
			param.setApprovalStatusCd(appLindDetail.get(i-1).getApprovalStatusCd());
			param.setApprovalGradeCd(appLindDetail.get(i-1).getApprovalGradeCd());
			dao.insertDocsDestroyRequestDetail(param);
		}
		//INSERT DOCS_DESTORY_REQUEST_MAPPING
		for(Map<String, Object> tempMap : arrList) {
			param.setRequestNo(tempMap.get("requestNo").toString());
			param.setObjectId(tempMap.get("objectId").toString());
			dao.insertDocsDestoryRequestMapping(param);
		}
		dao.updateApprovalFile(param);
	}

	public void formatLogType(GridResultVO result) {
		Object contents = result.getContents();

		if (contents instanceof List<?>) {
			for (Object item : (List<?>) contents) {
				try {
					Field logTypeField = item.getClass().getDeclaredField("logType");
					Field revisionField = item.getClass().getDeclaredField("revision");
					logTypeField.setAccessible(true); // private 필드 접근 가능하도록 설정
					revisionField.setAccessible(true);

					Object logType = logTypeField.get(item);
					Object revision = revisionField.get(item);
					if ("VIEWING".equals(logType)) {
						logTypeField.set(item, "열람");
					} else if ("PRINT".equals(logType)) {
						logTypeField.set(item, "출력");
					}

					if("null".equals(revision)) {
						revisionField.set(item, "-");
					}

				} catch (NoSuchFieldException | IllegalAccessException e) {
					System.err.println("Failed to access logType: " + e.getMessage());
				}
			}
		} else {
			System.err.println("contents is not a List.");
		}
	}

}
