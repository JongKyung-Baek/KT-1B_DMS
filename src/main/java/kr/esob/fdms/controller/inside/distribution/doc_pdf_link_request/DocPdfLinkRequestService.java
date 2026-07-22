package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.distribution.docpdfrequest.DocPdfRequestDao;
import kr.esob.fdms.controller.inside.distribution.docpdfrequest.DocPdfRequestParam;
import kr.esob.fdms.controller.inside.distribution.docpdfrequest.DocPdfRequestVO;

@Service
public class DocPdfLinkRequestService implements CommonService{

	@Inject
	DocPdfRequestDao dao;

	@Override
	public List selectList(Object param) {
		List<DocPdfRequestVO> rtnList = dao.selectList(param);
//		for(int i=0; i<rtnList.size(); i++){
//			String filePath = "";
//			//파일PATH 구분자가 다른경우 통일을 위한 작업
//			if( !(null==rtnList.get(i).getFilePath()) && !("".equals(rtnList.get(i).getFilePath()))) {
//				if(rtnList.get(i).getFilePath().contains("/")) {
//					rtnList.get(i).setFilePath(rtnList.get(i).getFilePath().replaceAll("/", "\\\\"));
//				}
//				if("".equals(filePath)) {
//					filePath = "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//				}else {
//					filePath += "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//				}
//				rtnList.get(i).setFilePath(filePath.replaceAll("\\\\", "\\\\\\\\\\\\\\\\"));
//			}
//		}
		return rtnList;
		
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

//	public void updateList(Object param) {
//		dao.updateList(param);
//	}

	public void deleteList(Object param) {

	}

	public void setSearchAllParam(DocPdfRequestParam param) {
		if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
			Gson gson = new Gson();
			param.setDocumentList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DocPdfRequestVO>>() {}.getType()));
		}
	}

}