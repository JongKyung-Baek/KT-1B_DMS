package kr.esob.fdms.controller.inside.distribution.annotationinfo;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusListVO;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusPopupParam;
//import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusPopupVO;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestParam;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnnotationInfoDao extends AbstractDao {
    private String prefix = "sql.AnnotationInfo.";


    @SuppressWarnings("unchecked")
    public List<AnnotationInfoListVO> selectList(Object param){
        return list(prefix + "selectList", param);
    }

    public Integer selectListCount(Object param){
        return (Integer) obj(prefix + "selectListCount", param);
    }

    public int selectPopupListCount(AnnotationInfoPopupParam param) {
        return (Integer) obj(prefix + "selectPopupListCount", param);
    }


    //	2023.07.04 천기범 추가
    public List<AnnotationInfoListVO> selectAnnotationPopupList(AnnotationInfoPopupParam param) {
        return list(prefix + "selectAnnotationPopupList", param);
    }


}
