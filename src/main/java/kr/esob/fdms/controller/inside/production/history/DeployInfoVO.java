package kr.esob.fdms.controller.inside.production.history;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployInfoVO {
	private String requestNo;
	private String objectId;
	private String objectNo;
	private String objectNm;
	private String revNo;
	private int deployCount;
	private int destroyCount;
	private int totalPageCnt;
	private String productNo;
	private String productNm;
	private String destroyStatusCd;
	private String fileNm;
	private int fileNo;

}
