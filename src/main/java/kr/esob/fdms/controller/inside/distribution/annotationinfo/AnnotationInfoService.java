package kr.esob.fdms.controller.inside.distribution.annotationinfo;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusDao;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoDao;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusPopupParam;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupParam;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusPopupVO;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupVO;

import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestParam;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestVO;
import kr.esob.fdms.util.DateUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class AnnotationInfoService implements CommonService{

    @Inject
    AnnotationInfoDao dao;

    @Inject
    DateUtil dateUtil;

    @SuppressWarnings("rawtypes")
    @Override
    public List selectList(Object param) {
        return dao.selectList(param);
    }

    @Override
    public int selectListCount(Object obj) {
        return dao.selectListCount(obj);
    }

    public int selectPopupListCount(AnnotationInfoPopupParam param) {
        return dao.selectPopupListCount(param);
    }
    //	2023.07.04 천기범 추가
    public List<AnnotationInfoListVO> selectAnnotationPopupList(AnnotationInfoPopupParam param) {
        return dao.selectAnnotationPopupList(param);
    }
}
