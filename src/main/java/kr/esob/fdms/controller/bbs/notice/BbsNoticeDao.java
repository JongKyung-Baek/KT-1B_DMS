package kr.esob.fdms.controller.bbs.notice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class BbsNoticeDao extends AbstractDao {
	private String prefix = "sql.notice.";


	@SuppressWarnings("unchecked")
	public List<BbsNoticeListVO> selectList(Object param){
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
	public List<BbsNoticeListVO> selectMainList(Object param){
		return list(prefix + "selectMainList", param);
	}

	public int selectPopupListCount(BbsNoticePopupParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

	public BbsNoticePopupVO getRequestInfo(BbsNoticePopupParam param) {
		return (BbsNoticePopupVO) obj(prefix + "getRequestInfo", param);
	}

	public Integer updateHitCount(Object hitCount) {
		return (Integer) obj(prefix + "updateHitCount", hitCount);
	}

	public void insertNoticeInfo(BbsNoticePopupParam param) {
		insert(prefix + "insertNoticeInfo", param);
	}

	public BbsNoticePopupParam selectNoticeInfo(BbsNoticePopupParam param) {
		return (BbsNoticePopupParam) obj(prefix + "selectNoticeInfo", param);
	}

	public void deleteNotice(BbsNoticePopupParam param) {
		update(prefix + "deleteNotice", param);
		update(prefix + "deleteNoticeFile", param);
	}

	public BbsNoticePopupVO addNoticeInfo(BbsNoticeAddParam param) {
		return (BbsNoticePopupVO) obj(prefix + "addNoticeInfo", param);
	}

	public void updateNoticeInfo(BbsNoticePopupParam param) {
		update(prefix + "updateNoticeInfo", param);
	}

	public void insertNoticeFile(BbsNoticePopupParam param) {
		insert(prefix + "insertNoticeFile", param);
	}

	public BbsNoticeFileVO selectFileInfo(BbsNoticeFileVO param) {
		return (BbsNoticeFileVO) obj(prefix + "selectFileInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<BbsNoticeFileVO> selectFileList(BbsNoticeFileVO param) {
		return list(prefix + "selectFileInfo", param);
	}

	public BbsNoticeFileVO selectFilePath(BbsNoticeFileVO param) {
		return (BbsNoticeFileVO) obj(prefix + "selectFilePath", param);
	}

	@SuppressWarnings("unchecked")
	public List<BbsNoticeFileVO> selectNoticeFileList(BbsNoticePopupParam param){
		return list(prefix + "selectNoticeFileList", param);
	}

	public BbsNoticePopupVO selectNoticePopupInfo(BbsNoticePopupParam param) {
		return (BbsNoticePopupVO) obj(prefix + "selectNoticePopupInfo", param);
	}

	/**
	 * 메인화면에 띄워줄 공지사항
	 * @return
	 */
	public BbsNoticePopupVO selectAlertNotice() {
		Map<String, Object> param = new HashMap<>();
		return (BbsNoticePopupVO) obj(prefix + "selectAlertNotice", param);
	}

	/**
	 * 공지사항의 파일 삭제(DB)
	 * @param param
	 */
	public void deleteNoticeFile(BbsNoticePopupParam param) {
		delete(prefix + "deleteNoticeFile", param);
	}
}
