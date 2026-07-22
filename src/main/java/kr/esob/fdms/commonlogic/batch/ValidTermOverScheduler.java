package kr.esob.fdms.commonlogic.batch;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.esob.fdms.commonlogic.insa.InsaService;
import kr.esob.fdms.commonlogic.validTermOver.ValidTermOverService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ValidTermOverScheduler {
	@Inject
	ValidTermOverService service;
//
//	@Scheduled(cron = "0 0/1 * * * *")
//    public void run() throws Exception {
//		log.info("유효기간만료 시작");
//		service.run();
//		log.info("유효기간만료 종료");
//    }
}