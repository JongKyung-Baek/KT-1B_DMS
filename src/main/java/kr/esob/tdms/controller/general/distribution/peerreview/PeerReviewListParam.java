package kr.esob.tdms.controller.general.distribution.peerreview;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PeerReviewListParam extends CommonParam {
	private String peerreviewNo;
	private String peerreviewNm;
	private String statusCd;
	private String requestStartDt;
	private String requestEndDt;
}
