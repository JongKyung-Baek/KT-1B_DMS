package kr.esob.fdms.commonlogic.insa;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class InsaTest {

	@Inject
	InsaService service;

	@PostConstruct
	public void init() throws Exception {
//		service.insa();

	}

}
