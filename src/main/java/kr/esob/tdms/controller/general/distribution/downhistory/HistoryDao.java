package kr.esob.tdms.controller.general.distribution.downhistory;


import java.util.List;
import java.util.Map;

import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.tdms.controller.general.distribution.printhistory.HistoryListParam;
import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.general.distribution.commonrequest.ApprovalLineDetailVO;

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
