package kr.esob.tdms.controller.general.distribution.viewprinthistory;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String distributionType;
	private String drawingNo;
	private String objectId;
	private String orgFileNm;
	private String insertDt;
	private String revision;
	private String userNo;
	private String userNm;
	private String logType;
	private String userId;
}
