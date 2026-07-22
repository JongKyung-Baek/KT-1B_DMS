package kr.esob.fdms.commonlogic.excel;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.grid.GridInfoVO;

/**
 * 리스트 화면과는 다르게, 엑셀 출력은 한군데서 다하기 때문에 VO로 할 수가 없어서 Map으로 처리
 * @author younjh
 *
 */
@Repository
public class CreateExcelDao extends AbstractDao {
	@SuppressWarnings("unchecked")
	public List<GridInfoVO> selectGridInfo(Map<String, String> paramMap) {
		return list("sql.Excel.selectList", paramMap);
	}
	
	@SuppressWarnings("unchecked")
	public List<GridInfoVO> selectGridDuanzongPdmInfo(Map<String, String> paramMap) {
		return list("sql.Excel.selectListDuanzongPdm", paramMap);
	}

	@SuppressWarnings("rawtypes")
	public List selectListForExcel(Map<String, String> paramMap){
		return list(paramMap.get("listId").toString(), paramMap);
	}

	public int selectCountForExcel(String queryId, Map<String, String> paramMap) {
		return (Integer) obj(queryId, paramMap);
	}
}
