package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionHistoryVO {
	private String objectNo;
	private String objectType;
	private String deployUserCd;
	private String deployUserNm;
	private String deployUserFullNm;

	private List<HistoryDetailVO> list;

}
