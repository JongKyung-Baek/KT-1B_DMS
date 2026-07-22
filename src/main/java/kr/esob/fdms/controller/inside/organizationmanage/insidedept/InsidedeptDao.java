package kr.esob.fdms.controller.inside.organizationmanage.insidedept;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InsidedeptDao extends AbstractDao {
	private String prefix = "sql.OrganizationmanageInsidedept.";


	@SuppressWarnings("unchecked")
	public List<InsidedeptListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public InsidedeptListVO selectDeptInfo() {
		return (InsidedeptListVO) obj(prefix + "selectDeptInfo");
	}

	public void insertRegisterDeptInfo(DeptPopupParam param) {
		insert(prefix + "insertRegisterDeptInfo", param);
	}

	public DeptListVO selectUser(String userCd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userCd", userCd);

		return (DeptListVO) obj(prefix + "selectUser", param);
	}

	public void editDeptInfo(DeptPopupParam param) {
		System.out.println("param in editDeptInfo: " + param.toString() );
		update(prefix + "editDeptInfo", param);
	}

	public DeptListVO selectDept(String deptCd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("deptCd", deptCd);

		return (DeptListVO) obj(prefix + "selectDept", param);
	}

	public Integer countUsersByDeptCd(Object param){
		return (Integer) obj(prefix + "countUsersByDeptCd", param);
	}
}
