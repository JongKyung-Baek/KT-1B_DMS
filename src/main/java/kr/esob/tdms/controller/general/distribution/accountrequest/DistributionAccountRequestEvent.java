package kr.esob.tdms.controller.general.distribution.accountrequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionAccountRequestEvent {
    private Long requestEventId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String actorType;
    private String actorId;
    private String actorName;
    private String comment;
    private String occurredAt;
}
