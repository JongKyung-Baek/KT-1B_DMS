package kr.esob.fdms.controller.inside.organizationmanage.approval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Service
public class ApprovalService implements CommonService {

	@Inject
	ApprovalDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public ApprovalListParam selectDetailInfo(ApprovalListParam param) {
		return dao.selectDetailInfo(param);
	}


	public ResultVO approvalUser(ApprovalListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();

		param.setStatusCd("APPROVAL");
		param.setRejectReason("");

		dao.updateReqeust(param);

		if (param.getRequestType().equals("I")) {
			dao.insertUser(param);
		}
		else if (param.getRequestType().equals("U")) {
			dao.updateUserInfo(param);
		}
		else {
			dao.deleteUserInfo(param);
		}

		if (!param.getRequestType().equals("D")) {
			if (param.getProtectYn().equals("Y")) {
				dao.updateUserProtectN(param);
				dao.updateUserProtectY(param);
			}

			if (param.getCrYn().equals("Y")) {
				dao.updateUserCr(param);
			}
		}

		resultVo.setSuccess(true);

		return resultVo;
	}

	public ResultVO rejectUser(ApprovalListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();

		param.setStatusCd("REJECT");
		dao.updateReqeust(param);

		resultVo.setSuccess(true);

		return resultVo;
	}

	public List<ComboInfoVO> venderUser(ApprovalListParam param) throws Exception {
		return dao.venderUser(param);
	}
}
