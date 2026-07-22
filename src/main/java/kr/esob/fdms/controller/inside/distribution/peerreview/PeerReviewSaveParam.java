package kr.esob.fdms.controller.inside.distribution.peerreview;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PeerReviewSaveParam extends CommonParam {
	private String objectId;
	private String cnSerial;
	private String peerreviewNo;
	private String peerreviewNm;
	private String reviewdate;
	private String revNo;
	private String approver;
	private String revieweruser;
	private String status;
	private String fileNm;
	private String orgFileNm;
	private String filePathNm;
	private String fileSize;
	private String currentPageNo;
	private String totalPageNo;
	private String requestNo;
}
