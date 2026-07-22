package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.convert.ConvertLogDao;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestDao;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestParam;
import kr.esob.fdms.controller.outside.drawing.approvalstatus.ApprovalStatusDao;
import kr.esob.fdms.controller.outside.drawing.approvalstatus.ApprovalStatusListVO;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@Log4j2
public class VersionCheckService implements CommonService{

    @Inject
    DrawingRequestDao dao;

    @Autowired
    DocPdfLinkRequestDao pdfDao;

    @Autowired
    ConvertLogDao convertLogDao;

    @Inject
    ApprovalStatusDao approvalStatusDao;

    @Override
    public List selectList(Object param) {
        List<ApprovalStatusListVO> rtnList = approvalStatusDao.selectPopupListOutside(param);
        return rtnList;
    }

    @Override
    public int selectListCount(Object param) {
        return approvalStatusDao.selectListCount(param);
    }

    public void setSearchAllParam(DrawingRequestParam param) {
        if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
            Gson gson = new Gson();
            param.setDrawingList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DrawingInfoVO>>() {}.getType()));
        }
    }

}


