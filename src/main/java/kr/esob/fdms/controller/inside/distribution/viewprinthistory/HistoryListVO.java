package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String distributionType;
	private String drawingNo;
	private String objectId;
	private String orgFileNm;
	private String insertDt;
	private String revision;
	private String userId;
	private String userNm;
	private String logType;
}
