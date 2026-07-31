package kr.esob.fdms.commonlogic.validTermOver;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

@Service
public class ValidTermOverService {

	@Inject
	ValidTermOverDao dao;

	public void run() throws Exception {
		try {

			ValidTermOverListVO param = new ValidTermOverListVO();

			List<ValidTermOverListVO> list = dao.selectList(param);

			for(ValidTermOverListVO tmp : list) {
				dao.updateValidTermOver(tmp);
			}

			dao.updateValidTermOverNoReg();

			dao.updateValidTermOverOldHistory();

		}
		catch(Exception e) {
			
			
			throw new Exception(e.getMessage());
		}
	}
}
