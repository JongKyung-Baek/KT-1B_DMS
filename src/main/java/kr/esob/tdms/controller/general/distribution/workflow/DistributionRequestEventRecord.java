package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

/**
 * Server-owned status transition history exposed with request details.
 */
@Getter
@Setter
public class DistributionRequestEventRecord {
    private Long eventId;
    private Long requestId;
    private String fromStatus;
    private String toStatus;
    private String eventType;
    private String actorUserCd;
    private String actorUserId;
    private String actorUserNm;
    private String eventComment;
    private String occurredAt;
}
