package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributionAccountRequestRecord {
    private Long requestId;
    private String eventId;
    private String correlationId;
    private String clientId;
    private String sourceSystemId;
    private String requestType;
    private String occurredAt;
    private String receivedAt;
    private String status;
    private String representativeId;
    private String representativeName;
    private String representativeEmail;
    private String representativePhone;
    private String organizationCode;
    private String organizationName;
    private String businessNumber;
    private String targetUserId;
    private String targetUserName;
    private String targetUserEmail;
    private String targetUserPhone;
    private String targetUserPosition;
    private String reason;
    private String metadataJson;
    @JsonIgnore
    private String contentSha256;
    private String decisionComment;
    private String decidedByUserCd;
    private String decidedByUserId;
    private String decidedByUserName;
    private String decidedAt;
    private String updatedAt;
    private Boolean duplicate;
    private List<DistributionAccountRequestEvent> events;
}
