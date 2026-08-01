package kr.esob.tdms.controller.general.distribution.downhistory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.tdms.controller.general.distribution.downhistory.HistoryDao;
import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.distribution.commonrequest.ApprovalLineDetailVO;


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
