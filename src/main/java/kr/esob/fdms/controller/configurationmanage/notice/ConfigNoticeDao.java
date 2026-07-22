package kr.esob.fdms.controller.configurationmanage.notice;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ConfigNoticeDao extends AbstractDao {
	private String prefix = "sql.configNotice.";


	@SuppressWarnings("unchecked")
	public List<ConfigNoticeListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
	
	public void insertConfigNotice(ConfigNoticeAddParam param) {
		insert(prefix + "insertConfigNotice", param);
	}
	
	public void insertConfigNoticeFile(ConfigNoticeAddParam param) {
		insert(prefix + "insertConfigNoticeFile", param);
	}
	
	public ConfigNoticePopupVO getNoticeInfo(ConfigNoticePopupVO param) {
		return (ConfigNoticePopupVO) obj(prefix + "getNoticeInfo", param);
	}
	
	public Integer updateHitCount(Object hitCount) {
		return (Integer) obj(prefix + "updateHitCount", hitCount);
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigNoticePopupVO> selectNoticeFileList(ConfigNoticePopupVO param){
		return list(prefix + "selectFileInfo", param);
	}
	
	public void updateConfigNotice(ConfigNoticeAddParam param) {
		insert(prefix + "updateNoticeInfo", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigNoticeAddParam> selectFileList(ConfigNoticeAddParam param) {
		return list(prefix + "selectFileInfo", param);
	}

	public void deleteNoticeFile(ConfigNoticeAddParam param) {
		insert(prefix + "deleteNoticeFile", param);
	}
	
	public void deleteNotice(ConfigNoticePopupVO param) {
		delete(prefix + "deleteNoticeFile", param);
		delete(prefix + "deleteNotice", param);
	}
	
	public ConfigNoticeAddParam selectFileInfo(ConfigNoticeAddParam param) {
		return (ConfigNoticeAddParam) obj(prefix + "selectFileInfo", param);
	}
}
