package kr.esob.fdms.commonlogic.batch;

import kr.esob.fdms.commonlogic.FromAdapToItn.FromAdapToItnService;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
@Slf4j
@ConditionalOnProperty(
		name = "send.mail.scheduler.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class SendMailScheduler {
	@Inject
	DocsMailService service;

	@Inject
	FromAdapToItnService itnService;

//	@Scheduled(cron = "* 0/15 * * * *")
//    public void run() throws Exception {
//		System.out.println("#######################");
//		System.out.println("MAIL SEND :: MAIL SEND :: MAIL SEND :: MAIL SEND ");
//		service.sendMail();
//		System.out.println("END :: END :: END :: END ");
//		System.out.println("#######################");
	@Scheduled(cron = "0 */15 * * * *")
	@PostConstruct
	public void run() throws Exception {
		log.info("#######################");
		log.info("MAIL SEND :: MAIL SEND :: MAIL SEND :: MAIL SEND ");
		itnService.mergeMails();
		log.info("END :: END :: END :: END ");
		log.info("#######################");
	}
}
