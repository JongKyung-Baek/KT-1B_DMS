package kr.esob.fdms.controller.outside.user.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;

@Service
public class StatusService implements CommonService {

	@Inject
	StatusDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public StatusListParam selectInformationDetailInfo(StatusListParam param) {
		StatusListParam vo = dao.selectInformationDetail(param);
		return vo;
	}
}
