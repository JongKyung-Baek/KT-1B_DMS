package kr.esob.tdms.commonlogic.batch;

import kr.esob.tdms.commonlogic.distributionDept.DeptInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@ConditionalOnProperty(
        name = "update.distribution.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class UpdateDistributionDeptScheduler {

    @Autowired
    DeptInfoService service;

    @Scheduled(cron = "0 */5 * * * *")
    @PostConstruct
    public void run() throws Exception {
        log.info("---------Updating Distribution Dept Info---------");
        service.moveData();
        log.info("-------------------------------------------------");
    }
}
