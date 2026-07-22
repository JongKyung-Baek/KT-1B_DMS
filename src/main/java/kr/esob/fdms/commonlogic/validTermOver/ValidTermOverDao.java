package kr.esob.fdms.commonlogic.validTermOver;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ValidTermOverDao extends AbstractDao {
	private String prefix = "sql.ValidTermOver.";


	@SuppressWarnings("unchecked")
	public List<ValidTermOverListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public void updateValidTermOver(Object param) {
		update(prefix + "updateValidTermOver", param);
	}
	
	public void updateValidTermOverNoReg() {
		update(prefix + "updateValidTermOverNoReg", "");
	}	
	
	public void updateValidTermOverOldHistory() {
		update(prefix + "updateValidTermOverOldHistory", "");
	}
}
