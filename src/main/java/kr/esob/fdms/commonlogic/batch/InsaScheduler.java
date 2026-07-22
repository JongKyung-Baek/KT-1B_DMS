package kr.esob.fdms.commonlogic.batch;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.esob.fdms.commonlogic.insa.InsaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InsaScheduler {
	@Inject
	InsaService service;
//
//	@Scheduled(cron = "0 27 * * * *")
//    public void run() throws Exception {
//		log.info("인사연동 시작");
//		service.insa();
//		log.info("인사연동 종료");
//    }
}