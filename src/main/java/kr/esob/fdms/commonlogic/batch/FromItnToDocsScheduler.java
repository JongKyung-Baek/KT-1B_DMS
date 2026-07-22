package kr.esob.fdms.commonlogic.batch;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.esob.fdms.commonlogic.FromItnToDocs.FromItnToDocsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FromItnToDocsScheduler {
	@Inject
	FromItnToDocsService service;

//	@Scheduled(cron = "0/15 * * * * *")
//    public void run() throws Exception {
//		log.info("Updating Docs from ITN");
//		service.transferItnToDocs();
//		log.info("Complete Updating Docs from ITN");
//    }
	
}
