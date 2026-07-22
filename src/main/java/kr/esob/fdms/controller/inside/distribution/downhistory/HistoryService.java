package kr.esob.fdms.controller.inside.distribution.downhistory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.fdms.controller.inside.distribution.downhistory.HistoryDao;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;


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

    public List<HistoryListVO> selectDownHistoryPopupList(DownListParam param) {
        return dao.selectDownHistoryPopupList(param);
    }

    public List<HistoryListVO> selectActLogPopupList(DownListParam param) {
        return dao.selectActLogPopupList(param);
    }



}
