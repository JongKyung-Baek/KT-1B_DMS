package kr.esob.tdms.controller.general.distribution.workflow;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Materializes the EXPIRED state for audit history. List queries also exclude
 * elapsed rows directly, so visibility never depends on scheduler timing.
 */
@Component
public class DistributionExpirationScheduler {
    private final DistributionWorkflowService workflowService;

    public DistributionExpirationScheduler(DistributionWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Scheduled(cron = "0 5 * * * *", zone = "Asia/Seoul")
    public void expireElapsedDistributions() {
        workflowService.expireApprovedRequests();
    }
}
