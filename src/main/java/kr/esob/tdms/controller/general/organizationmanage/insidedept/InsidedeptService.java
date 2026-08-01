package kr.esob.tdms.controller.general.organizationmanage.insidedept;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.List;

@Service
public class InsidedeptService implements CommonService {

	@Inject
	InsidedeptDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveRegsiterDept(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		String saveFlag = request.getParameter("saveFlag");
		String deptCd = request.getParameter("deptCd");
		String deptNm = request.getParameter("deptNm");
		String useYn = "N".equals(request.getParameter("useYn")) ? "N" : "Y";
		String delYn = "N";
		String sortSeq = "20";

		if (deptNm == null || deptNm.trim().isEmpty()) {
			resultVo.setMessage("msg.selectDeptNm");
			resultVo.setSuccess(false);
			return resultVo;
		}
		deptNm = deptNm.trim();

		if (!"I".equals(saveFlag) && !"U".equals(saveFlag)) {
			resultVo.setMessage("msg.error");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if ("U".equals(saveFlag)
				&& (deptCd == null || deptCd.trim().isEmpty())) {
			resultVo.setMessage("msg.selectDeptNm");
			resultVo.setSuccess(false);
			return resultVo;
		}

		// Serialize code allocation and duplicate-name checks within this
		// transaction so concurrent administrators cannot create collisions.
		dao.lockDepartmentMutation();

		if ("I".equals(saveFlag)) {
			InsidedeptListVO deptInfo = dao.selectDeptInfo();
			DeptPopupParam deptPopupParam = DeptPopupParam.builder()
					.deptCd(generateNextDeptCd(deptInfo))
					.deptNm(deptNm)
					.useYn(useYn)
					.delYn(delYn)
					.sortSeq(sortSeq)
					.build();

			if (dao.countDeptByName(deptNm, null) > 0) {
				resultVo.setMessage("msg.alrExistDept"); // "이미 존재하는 부서입니다."
				resultVo.setSuccess(false);
				return resultVo;
			}

			dao.insertRegisterDeptInfo(deptPopupParam);
			resultVo.setSuccess(true);
			return resultVo;
		}

		if ("U".equals(saveFlag)) {
			DeptPopupParam deptPopupParam = DeptPopupParam.builder()
					.deptCd(deptCd)
					.deptNm(deptNm)
					.useYn(useYn)
					.delYn(delYn)
					.sortSeq(sortSeq)
					.build();

			if (dao.countDeptByName(deptNm, deptCd) > 0) {
				resultVo.setMessage("msg.alrExistDept");
				resultVo.setSuccess(false);
				return resultVo;
			}

			// 현재 부서에 소속된 사용자가 있는데 useYn 값을 N으로 변경 시 실패
			if ("N".equals(useYn)) {
				int deptUsers = dao.countUsersByDeptCd(deptPopupParam);
				if (deptUsers > 0) {
					resultVo.setMessage("msg.userExist");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			dao.editDeptInfo(deptPopupParam);
			resultVo.setSuccess(true);
			return resultVo;
		}

		resultVo.setSuccess(false);
		return resultVo;
	}

	private String generateNextDeptCd(InsidedeptListVO deptInfo) {
		String maxDeptCd = deptInfo == null ? null : deptInfo.getDeptCd();
		if (maxDeptCd == null || maxDeptCd.trim().isEmpty()) {
			return "DMS001";
		}

		String letterPart = maxDeptCd.replaceAll("\\d+$", "");
		String numberPart = maxDeptCd.substring(letterPart.length());
		int newNumber = Integer.parseInt(numberPart) + 1;
		return letterPart + String.format("%03d", newNumber);
	}


	public DeptListVO selectDept(String deptCd) {
		return dao.selectDept(deptCd);
	}
}






