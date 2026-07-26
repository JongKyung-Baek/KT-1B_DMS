package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryEventVO {
    private String historyType;
    private String occurredAt;
    private String completedAt;
    private String actorUserCd;
    private String actorUserId;
    private String actorUserNm;
    private String distributionType;
    private String drawingNo;
    private String objectType;
    private String objectId;
    private String orgFileNm;
    private String revision;
    private String printJobId;
    private Integer itemSeq;
    private Integer itemCount;
    private String fileNo;
    private String requestNo;
    private String requestType;
    private String statusCd;
    private Integer pageCount;
    private Integer copyCount;
    private String printerNm;
    private String deviceId;
    private String clientIp;
    private String errorMessage;
}
