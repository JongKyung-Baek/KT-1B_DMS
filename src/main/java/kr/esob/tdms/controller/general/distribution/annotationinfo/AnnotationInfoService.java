package kr.esob.tdms.controller.general.distribution.annotationinfo;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
//import kr.esob.tdms.controller.general.distribution.requeststatus.RequestStatusDao;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoDao;
//import kr.esob.tdms.controller.general.distribution.requeststatus.RequestStatusPopupParam;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoPopupParam;
//import kr.esob.tdms.controller.general.distribution.requeststatus.RequestStatusPopupVO;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoPopupVO;

import kr.esob.tdms.controller.general.distribution.commonrequest.CommonDistributionRequestParam;
import kr.esob.tdms.controller.general.distribution.commonrequest.CommonDistributionRequestVO;
import kr.esob.tdms.util.DateUtil;
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
