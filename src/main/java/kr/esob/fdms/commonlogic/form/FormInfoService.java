package kr.esob.fdms.commonlogic.form;


import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboParamVO;

@Service
public class FormInfoService {

	@Inject
	FormInfoDao dao;

	ComboDao comboDao;

	public List<FormInfoVO> selectFormInfo(String formId) {
		return dao.selectFormInfoList(formId);
	}

	public List<ComboInfoVO> selectQueryList(ComboParamVO comboParamVO) {
		return dao.selectQueryComboList(comboParamVO);
	}

	public List<ComboInfoVO> getComboList(Object param) {
		return comboDao.selectComboList(param);
	}

}
