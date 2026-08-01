package kr.esob.tdms.controller.general.production.disposalstatus;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.mail.DocsMailEnum;
import kr.esob.tdms.commonlogic.mail.DocsMailService;
import kr.esob.tdms.commonlogic.mail.MailInfoVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.distribution.commonrequest.ApprovalLineDetailVO;

@Service
public class DisposalStatusService implements CommonService{

	@Inject
	DisposalStatusDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}
}
