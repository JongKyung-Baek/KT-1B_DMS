package kr.esob.tdms.commonlogic.combo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.commonlogic.message.LocaleUtil;
import kr.esob.tdms.commonlogic.value.SessionValue;

@Repository
public class ComboDao extends AbstractDao {

	@Inject
	SessionValue sessionValue;

	public List<SearchComboInfoVO> selectComboListByCd(String comboCd){
		ComboInfoVO param = new ComboInfoVO();
		param.setComboCd(comboCd);
		param.setSessionLang(currentSessionLanguage());
		return listNotUseSession("sql.Combo.selectComboListByCd", param);
	}

	public List<ComboInfoVO> selectComboList(Object param){
		return listNotUseSession(
				"sql.Combo.selectComboList",
				withSessionLanguage(param));
	}

	//공동발행자 값조회
	public List<ComboInfoVO> selectActiveUserList() {
		return list("sql.Combo.selectActiveUserList");
	}

	public List<ComboCdVO> selectComboLang(){
		return selectComboLang(currentSessionLanguage());
	}

	public List<ComboCdVO> selectComboLang(String language){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("sessionLang", normalizeLanguage(language));
		return listNotUseSession("sql.Combo.selectComboLang", param);
	}

	@SuppressWarnings("unchecked")
	public List<SearchComboInfoVO> selectSearchComboList(SearchComboParamVO param){
		return list(param.getQueryId(), param);
	}
	
	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectCombo(Map<String, Object> paramMap) {
		Map<String, Object> localizedParam =
				new HashMap<String, Object>(paramMap);
		localizedParam.put("sessionLang", currentSessionLanguage());
		return listNotUseSession(
				localizedParam.get("queryId").toString(),
				localizedParam);
	}

	private Object withSessionLanguage(Object param) {
		String language = currentSessionLanguage();
		if (param == null) {
			ComboInfoVO localized = new ComboInfoVO();
			localized.setSessionLang(language);
			return localized;
		}
		if (param instanceof String) {
			ComboInfoVO localized = new ComboInfoVO();
			localized.setComboCd((String) param);
			localized.setSessionLang(language);
			return localized;
		}
		if (param instanceof ComboInfoVO) {
			((ComboInfoVO) param).setSessionLang(language);
			return param;
		}
		if (param instanceof CommonParam) {
			((CommonParam) param).setSessionLang(language);
			return param;
		}
		if (param instanceof Map) {
			Map<?, ?> source = (Map<?, ?>) param;
			Map<Object, Object> localized =
					new HashMap<Object, Object>(source);
			localized.put("sessionLang", language);
			return localized;
		}
		return param;
	}

	private String currentSessionLanguage() {
		return normalizeLanguage(
				sessionValue == null ? null : sessionValue.getSessionLang());
	}

	private String normalizeLanguage(String language) {
		String normalized = LocaleUtil.normalizeSupportedLanguage(language);
		return normalized == null
				? LocaleUtil.getDefaultLocale().getLanguage()
				: normalized;
	}
}
