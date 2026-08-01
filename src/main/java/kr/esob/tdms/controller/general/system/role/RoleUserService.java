package kr.esob.tdms.controller.general.system.role;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.login.DeptVO;

@Service
public class RoleUserService implements CommonService {
	@Inject
	RoleDao dao;

	/**
	 * 전체 사용자 목록
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object obj) {
		return dao.selectUser(obj);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectUserCount(obj);
	}
}
