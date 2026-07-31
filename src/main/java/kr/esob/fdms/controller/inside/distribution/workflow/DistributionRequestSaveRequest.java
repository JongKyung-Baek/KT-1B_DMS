package kr.esob.fdms.controller.inside.distribution.workflow;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestSaveRequest {
    private String title;
    private String purpose;
    private List<DistributionRequestItemRef> items;
}
