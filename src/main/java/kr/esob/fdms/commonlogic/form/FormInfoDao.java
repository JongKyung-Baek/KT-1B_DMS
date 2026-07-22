package kr.esob.fdms.commonlogic.form;


import java.util.List;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboParamVO;

@Repository
public class FormInfoDao extends AbstractDao {

	@SuppressWarnings("unchecked")
	public List<FormInfoVO> selectFormInfoList(String formId) {
		FormParamVO vo = new FormParamVO();
		vo.setFormId(formId);

		return list("sql.FormInfo.selectList", vo);
	}

	public List<ComboInfoVO> selectQueryComboList(ComboParamVO comboParamVO){
		return comboList(comboParamVO.getQueryId(), comboParamVO);
	}

}
