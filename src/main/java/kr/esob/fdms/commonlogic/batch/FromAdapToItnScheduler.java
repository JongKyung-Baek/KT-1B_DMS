package kr.esob.fdms.commonlogic.batch;

import kr.esob.fdms.commonlogic.FromAdapToItn.FromAdapToItnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Slf4j
@Component
@ConditionalOnProperty(
		name = "file.auto.insert.scheduler.enabled",
		havingValue = "true",
		matchIfMissing = true  // 설정이 없으면 기본적으로 활성화
)
public class FromAdapToItnScheduler {
	@Inject
	FromAdapToItnService service;

//	 @Scheduled(cron = "0/10 * * * * *")
	@Scheduled(cron = "0 */5 * * * *")
	@PostConstruct
    public void run() throws Exception {
		log.info("Updating ITN from ADAP");
		// service.transferAdapToItn();
// 		2023. 07. 25 기범 주석처리 ( 등록 기능만 사용. 스케쥴러는 일단 주석처리 )
		service.fileAutoInsertSchedule();
		log.info("Complete Updating ITN from ADAP");
    }
}
