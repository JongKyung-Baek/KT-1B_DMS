package kr.esob.fdms.commonlogic.combo;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ComboDao extends AbstractDao {

	public List<SearchComboInfoVO> selectComboListByCd(String comboCd){
		return list("sql.Combo.selectComboListByCd", comboCd);
	}

	public List<ComboInfoVO> selectComboList(Object param){
		return list("sql.Combo.selectComboList", param);
	}

	//공동발행자 값조회
	public List<ComboInfoVO> selectActiveUserList() {
		return list("sql.Combo.selectActiveUserList");
	}

	public List<ComboCdVO> selectComboLang(){
		return list("sql.Combo.selectComboLang");
	}

	@SuppressWarnings("unchecked")
	public List<SearchComboInfoVO> selectSearchComboList(SearchComboParamVO param){
		return list(param.getQueryId(), param);
	}
	
	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectCombo(Map<String, Object> paramMap) {
		return list(paramMap.get("queryId").toString(), paramMap);
	}
}
