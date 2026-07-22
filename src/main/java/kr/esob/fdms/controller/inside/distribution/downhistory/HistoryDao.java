package kr.esob.fdms.controller.inside.distribution.downhistory;


import java.util.List;
import java.util.Map;

import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.fdms.controller.inside.distribution.printhistory.HistoryListParam;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;

@Repository
public class HistoryDao extends AbstractDao{

    private String prefix = "sql.DownHistory.";

    @SuppressWarnings("unchecked")
    public List<HistoryListParam> selectList(Object param){
        return list(prefix + "selectList", param);
    }

    public Integer selectListCount(Object param){
        return (Integer) obj(prefix + "selectListCount", param);
    }


    public List<HistoryListVO> selectDownHistoryPopupList(DownListParam param) {
        return list(prefix + "selectDownHistoryPopupList", param);
    }
    public List<HistoryListVO> selectActLogPopupList(DownListParam param) {
        return list(prefix + "selectActLogPopupList", param);
    }
}
