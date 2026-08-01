package kr.esob.tdms.controller.main;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

@Service
public class MainService {

	@Inject
	MainDao dao;

	public Integer selectSessionTime() {
		return dao.selectSessionTime();
	}

	public int updateSessionTime(int sessionTime) {
		return dao.updateSessionTime(sessionTime);
	}
}
