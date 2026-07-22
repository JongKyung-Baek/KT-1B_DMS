package kr.esob.fdms.controller.bbs.qna;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;
import net.sf.json.JSONObject;

@Service
public class BbsQnaService implements CommonService{

	@Inject
	BbsQnaDao dao;

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

	/**
	 * 메인화면용
	 * @param param
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List selectMainList(Object param) {
		List<BbsQnaListVO> list = dao.selectMainList(param);
		List<BbsQnaListVO> result = new ArrayList<BbsQnaListVO>();

		for(BbsQnaListVO qna : list) {

			result.add(qna);

			List<BbsQnaListVO> replyList = dao.selectMainReplyList(qna);

			if(null != replyList && replyList.size() > 0) {
				for(BbsQnaListVO reply : replyList) {
					result.add(reply);
				}
			}
		}

		return result;
	}

	/**
	 * 메인화면용 댓글
	 * @param param
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List selectMainReplyList(Object param) {
		return dao.selectMainReplyList(param);
	}

	public BbsQnaPopupVO getRequestInfo(BbsQnaPopupParam param) {
		return dao.getRequestInfo(param);
	}

	public Integer updateHitCount(Object param) {
		return dao.updateHitCount(param);
	}


	public ResultVO insertBbsQna(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		String paramStr = request.getParameter("bbsParam");
		BbsQnaAddParam bbsParam = (BbsQnaAddParam) ObjectUtil.jsonToObj(request.getParameter("bbsParam"), BbsQnaAddParam.class);
		bbsParam.setQnaTitle(StringUtil.encodeHTML(bbsParam.getQnaTitle()));
		bbsParam.setContents(StringUtil.encodeHTML(bbsParam.getContents()));
		dao.insertQnaInfo(bbsParam);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public BbsQnaPopupVO addQnaInfo(BbsQnaAddParam param) {
		return dao.addQnaInfo(param);
	}

	public BbsQnaPopupParam selectQnaInfo(BbsQnaPopupParam param) {
		return dao.selectQnaInfo(param);
	}

	public ResultVO saveQna(BbsQnaPopupParam param) {
		ResultVO result = new ResultVO();
		dao.deleteQna(param);
		result.setSuccess(true);
		return result;
	}

	public ResultVO replyBbsQna(MultipartHttpServletRequest request) {
		ResultVO resultVo = new ResultVO();
		BbsQnaReplyParam bbsParam = (BbsQnaReplyParam) ObjectUtil.jsonToObj(request.getParameter("replyParam"), BbsQnaReplyParam.class);

		bbsParam.setQnaTitle(StringUtil.encodeHTML(bbsParam.getQnaTitle()));
		bbsParam.setContents(StringUtil.encodeHTML(bbsParam.getContents()));
		dao.replyQnaInfo(bbsParam);

		resultVo.setSuccess(true);
		return resultVo;
	}
}