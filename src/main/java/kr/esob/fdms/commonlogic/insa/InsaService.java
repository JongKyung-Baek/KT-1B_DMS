package kr.esob.fdms.commonlogic.insa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class InsaService implements CommonService{

	@Inject
	InsaDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public void insa() throws Exception {
		try {

			// Step1. 부서연동

			// Step2. 사용자, 팀장 연동
			processUser();

			// 생산기술자료 결재권자
			processProtectApprovalUser();

			// Step3. vendor 연동 - 최초 1회만 연동함
			//processVendor();

			processRoleGroup();

			processRoleGroupDept();

			processRoleGroupUser();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void processRoleGroup() {
		List<RoleGroupVO> list = dao.selectRoleGroupSetting();
		for(RoleGroupVO vo : list) {
			dao.updateRoleGroup(vo);
		}
	}

	public void processRoleGroupDept() {
		dao.updateRoleGroupDept();
	}

	public void processRoleGroupUser() {
		dao.updateRoleGroupUser();
	}

	public void processProtectApprovalUser() {
		if(dao.selectProtectApprovalUserCount() == 0) {
			// 데이터가 한건이라도 있으면 insert하지 않는다.
			dao.insertProtectApprovalUser();
		}
	}

	public void processUser() throws Exception {
		Integer records = dao.selectItnUserCount();
		Integer size = 10;

		Integer totalPage = (int) Math.ceil((double)records / size);

		Map<String, Object> itnParamMap = new HashMap<String, Object>();

		for(int page = 1; page < totalPage; page++) {
			Integer start = ((page -1) * size) + 1;
			Integer end = page * size;

			itnParamMap.put("start", start);
			itnParamMap.put("end", end);

			List<Map<String, Object>> itnUserList = dao.selectItnUser(itnParamMap);

			for(Map<String, Object> itnUser : itnUserList) {
				System.out.println((page + "/" + totalPage) + " 번째 페이지 진행 중");
				//	index++;
				Map<String, Object> docsUser = dao.selectDocsUser(itnUser);

//		System.out.println(JSONArray.fromObject(docsUser));

				Iterator<String> a = itnUser.keySet().iterator();

				while(a.hasNext()) {
					String key = a.next();
					String value = itnUser.get(key).toString();

					System.out.println(key + " : " + value + ", " + value.length());

				}

				if(null == docsUser) {
//			itnUser.put("USER_CD", "USER_" + String.format("%010d", index));
					itnUser.put("USER_CD", dao.selectUserCd());
//			itnUser.put("USER_CD", dao.selectUserCdFromUserId(itnUser));
					dao.insertDocsUser(itnUser);
				}
				else {
					itnUser.put("USER_CD", docsUser.get("USER_CD"));
					dao.updateDocsUser(itnUser);
				}

//		072 파트리더(파트장)
//		A60 팀장
				if(null != itnUser.get("JICHKCODE") && (itnUser.get("JICHKCODE").equals("072") || itnUser.get("JICHKCODE").equals("A60"))) {
					dao.updateTeamLeader(itnUser);
				}
			}
		}
	}

	/**
	 * 벤더 연동 - 사용자 먼저 연동 해야 함.
	 */
	public void processVendor() {
		// 최초 1회만 연동함. Y일때만 연동
		if(!"Y".equals(dao.selectMigrationVendorYn())) {
			return;
		}

		List<Map<String, Object>> vendorList = dao.selectItnVendor();

		for(Map<String, Object> vendor : vendorList) {
			Map<String, Object> company = dao.selectDocsCompany(vendor);
			if(null == company) {
				vendor.put("COMPANY_CD", dao.selectCompanyCd());
				dao.insertCompany(vendor);
			}
			else {
				vendor.put("COMPANY_CD", company.get("COMPANY_CD"));
				dao.updateCompany(vendor);
			}

			dao.deleteCompanyPurchaser(vendor);
			dao.insertCompanyPurchaser(vendor);
		}

		// 연동 끝나면 더이상 연동을 하지 않도록 'N'으로 변경
		dao.updateMigrationVendorYn();
	}
}
