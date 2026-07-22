package kr.esob.fdms.commonlogic.batch;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.esob.fdms.commonlogic.GarbageCleaner.GarbageCleanerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GarbageCleanerScheduler {
	@Inject
	GarbageCleanerService service;

	//@Scheduled(cron = "0/15 * * * * *")
	@Scheduled(cron = "0 12 0 * * *")	
    public void run() throws Exception {
		log.info("Starting MergeFile GarbageCleaner");
		service.GarbageCleaner();
		log.info("Complete MergeFile GarbageCleaner");
    }
	
}
