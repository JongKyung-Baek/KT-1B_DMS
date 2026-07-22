package kr.esob.fdms.controller.configurationmanage.qna;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ConfigQnaDao extends AbstractDao {
	private String prefix = "sql.configQna.";


	@SuppressWarnings("unchecked")
	public List<ConfigQnaListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
	
	public void insertQna(ConfigQnaAddParam param) {
		insert(prefix + "insertQna", param);
	}
	
	public void insertConfigQnaFile(ConfigQnaAddParam param) {
		insert(prefix + "insertConfigQnaFile", param);
	}
	
	public ConfigQnaPopupVO getQnaInfo(ConfigQnaPopupVO param) {
		return (ConfigQnaPopupVO) obj(prefix + "getQnaInfo", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigQnaPopupVO> selectQnaFileList(ConfigQnaPopupVO param){
		return list(prefix + "selectFileInfo", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigQnaAddParam> selectFileList(ConfigQnaAddParam param){
		return list(prefix + "selectFileList", param);
	}
	
	public void deleteQna(ConfigQnaPopupVO param) {
		delete(prefix + "deleteQnaFile", param);
		delete(prefix + "deleteQna", param);
	}
	
	public void deleteQnaFile(ConfigQnaAddParam param) {
		delete(prefix + "deleteQnaFile", param);
	}
	
	public ConfigQnaPopupVO selectFileInfo(ConfigQnaPopupVO param){
		return (ConfigQnaPopupVO) obj(prefix + "selectFileInfo", param);
	}
	
	public void updateConfigQna(ConfigQnaAddParam param) {
		update(prefix + "updateQnaInfo", param);
	}
	
	public void replyQna(ConfigQnaPopupVO param) {
		insert(prefix + "insertReplyQna", param);
	}
}
