package kr.esob.fdms.commonlogic.validTermOver;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;

@Service
public class ValidTermOverService {

	@Inject
	ValidTermOverDao dao;

	@Inject
	DocsMailService mailService;

	public void run() throws Exception {
		try {

			ValidTermOverListVO param = new ValidTermOverListVO();

			List<ValidTermOverListVO> list = dao.selectList(param);

			for(ValidTermOverListVO tmp : list) {
				dao.updateValidTermOver(tmp);
//				if(param.getSendEmailYn().isBooleanValue()) {
//					MailInfoVO mailInfoVo = mailService.selectDeployUserInfo(tmp);
//					mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_VALID_TERM_OVER);
//					mailService.sendDocsMail(mailInfoVo);
//				}
			}

			dao.updateValidTermOverNoReg();

			dao.updateValidTermOverOldHistory();

		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
}
