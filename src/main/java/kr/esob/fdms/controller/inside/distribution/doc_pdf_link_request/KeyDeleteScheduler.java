package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class KeyDeleteScheduler {

    @Scheduled(cron = "0 0 * * * ?")
    public void deleteKey(){
        // DOCS_VIEWER_KEY table removed: skip scheduled cleanup.
        log.debug("Skip deleteKey scheduler because DOCS_VIEWER_KEY is not used.");
    }

}
