package kr.esob.tdms.controller.general.distribution.workflow;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestDetail {
    private DistributionRequestRecord request;
    private List<DistributionRequestRecipientSnapshot> recipients = Collections.emptyList();
    private List<DistributionDocumentBundle> documents = Collections.emptyList();
    private List<DistributionRequestItemSnapshot> items = Collections.emptyList();
    private List<DistributionRequestEventRecord> events = Collections.emptyList();
}
