package kr.esob.fdms.commonlogic.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.combo.SearchComboParamVO;
import net.sf.json.JSONObject;

@Service
public class ComboService implements CommonService{

	@Inject
	ComboDao dao;

	@Override
	public List selectList(Object param) {
		return null;
	}

	@Override
	public int selectListCount(Object param) {
		return 0;
	}

	public Map<String, Object> selectSearchComboList(SearchComboParamVO vo) {
		int limit = 50;
		int page = (null == vo.getPage() ? 1 : vo.getPage());
		vo.setPosStart(limit * page - limit);
		vo.setCount(limit);

		Map<String, Object> result = new HashMap<String, Object>();

		List<SearchComboInfoVO> list = dao.selectSearchComboList(vo);
		List<SearchComboInfoVO> list2 = new ArrayList<SearchComboInfoVO>();

		SearchComboInfoVO plzSelect = new SearchComboInfoVO();

		plzSelect.setId("");
		plzSelect.setText("선택하세요");

		list2.add(plzSelect);
		list2.addAll(list);

		Map<String, Object> pagination = new HashMap<String, Object>();
		pagination.put("more", false);

		result.put("results", list2);
		result.put("pagination", pagination);

		return result;
	}

	public List<ComboInfoVO> selectComboList(ComboInfoVO combo) {
		return dao.selectComboList(combo);
	}

	public List<ComboInfoVO> selectComboList(String comboCd) {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd(comboCd);
		return dao.selectComboList(combo);
	}

	//공동발행자 값조회
	public List<ComboInfoVO> selectActiveUserList() {
		return dao.selectActiveUserList();
	}

}
