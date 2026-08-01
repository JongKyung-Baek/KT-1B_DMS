package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

/**
 * Client input deliberately contains identifiers only. File names, metadata and
 * paths are always resolved from the database by the server.
 */
@Getter
@Setter
public class DistributionRequestItemRef {
    private String objectType;
    private String objectId;
    private String fileNo;
}
