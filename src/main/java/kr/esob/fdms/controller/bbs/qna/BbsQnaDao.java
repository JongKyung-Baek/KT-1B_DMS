package kr.esob.fdms.controller.bbs.qna;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class BbsQnaDao extends AbstractDao {
	private String prefix = "sql.qna.";


	@SuppressWarnings("unchecked")
	public List<BbsQnaListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	/**
	 * 메인화면용
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BbsQnaListVO> selectMainList(Object param){
		return list(prefix + "selectMainList", param);
	}

	/**
	 * 메인화면용 댓글
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BbsQnaListVO> selectMainReplyList(Object param){
		return list(prefix + "selectMainReplyList", param);
	}

	//팝업 상세
		public BbsQnaPopupVO getRequestInfo(BbsQnaPopupParam param) {
			return (BbsQnaPopupVO) obj(prefix + "getRequestInfo", param);
		}

		public void insertQnaInfo(BbsQnaAddParam param) {
			insert(prefix + "insertQnaInfo", param);
		}

		public BbsQnaPopupVO addQnaInfo(BbsQnaAddParam param) {
			return (BbsQnaPopupVO) obj(prefix + "addQnaInfo", param);
		}

		public BbsQnaPopupParam selectQnaInfo(BbsQnaPopupParam param) {
			return (BbsQnaPopupParam) obj(prefix + "selectQnaInfo", param);
		}

		public void deleteQna(BbsQnaPopupParam param) {
			update(prefix + "deleteQna", param);
		}

		public void replyQnaInfo(BbsQnaReplyParam param) {
			insert(prefix + "replyQnaInfo", param);
		}

		public Integer updateHitCount(Object hitCount) {
			return (Integer) obj(prefix + "updateHitCount", hitCount);
		}
}
