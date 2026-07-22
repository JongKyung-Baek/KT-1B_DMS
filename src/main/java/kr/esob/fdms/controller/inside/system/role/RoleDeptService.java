package kr.esob.fdms.controller.inside.system.role;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.login.DeptVO;

@Service
public class RoleDeptService implements CommonService {
	@Inject
	RoleDao dao;

	/**
	 * 전체 부서 목록
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object obj) {
		return dao.selectDept(obj);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectDeptCount(obj);
	}
}
