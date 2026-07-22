package kr.esob.fdms.controller.inside.distribution.acceptance;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestVO;

@Service
public class AcceptancePopupListService implements CommonService{

	@Inject
	AcceptanceDao dao;

	@Override
	public List selectList(Object param) {		
//		List<AcceptanceVO> rtnList = dao.selectPopupList(param);
//		for(int i=0; i<rtnList.size(); i++){
//			String filePath = "";
//			//파일PATH 구분자가 다른경우 통일을 위한 작업
//			if(rtnList.get(i).getFilePath().contains("/")) {
//				rtnList.get(i).setFilePath(rtnList.get(i).getFilePath().replaceAll("/", "\\\\"));
//			}
//			if("".equals(filePath)) {
//				filePath = "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//			}else {
//				filePath += "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//			}
//			rtnList.get(i).setFilePath(filePath.replaceAll("\\\\", "\\\\\\\\\\\\\\\\"));
//		}
		List<AcceptanceVO> rtnList = dao.selectPopupList(param);
		AcceptanceParam tempParam = (AcceptanceParam) param;
		if( "MANUAL".equals(tempParam.getRequestPurposeData()) || "PRODUCT".equals(tempParam.getRequestPurposeData())) {
			for(int i=0; i<rtnList.size(); i++) {
				AcceptanceVO tempVo = rtnList.get(i);
				if( !(null==tempVo.getDistributeTypeCode()) ) {
					if("hdCADApprovedDrawing".equals(tempVo.getDistributeTypeCode().toString())) {	//승인도 일경우 60개월 표기
						tempVo.setUseEnd("60");
					}else {
						tempVo.setUseEnd("6");
					}
				}
			}
		}
		return rtnList;
		
	}

	@Override
	public int selectListCount(Object param) {
		return '0';
	}

	public Object getPopupListCount(AcceptanceParam param) {
		return dao.getPopupListCount(param);
	}
}
