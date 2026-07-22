package kr.esob.fdms.controller.outside.duanzong.docs;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class DocsDao extends AbstractDao {
	private String prefix = "sql.DuanzongDocs.";

	@SuppressWarnings("unchecked")
	public List<DocsListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<DocsDefensePursVO> getDefensePurs() {
		return list(prefix + "getDefensePurs");
	}
	
	public DocsDefensePursUserVO getDefensePursUser(Object param) {
		return (DocsDefensePursUserVO) obj(prefix + "getDefensePursUser", param);
	}
	
//	팝업상세보기 정보 가져오기
	public DocsStatusPopupVO getDuanzongDocs(String managementNo) {
		return (DocsStatusPopupVO) obj(prefix + "getDuanzongDocs", managementNo);
	}
	
//	관리번호 최근 값 가져오기
	public Integer managementNoMax(){
		return (Integer) obj(prefix + "managementNoMax");
	}
	
//	추가
	public void insertDocsDuanzong(DocsAddParam param) {
		insert(prefix + "insertDocsDuanzong", param);
	}
}