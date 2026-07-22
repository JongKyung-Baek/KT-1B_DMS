package kr.esob.fdms.controller.inside.distribution.peerreview;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeerReviewListVO {
	private String objectType;
	private String objectTypeNm;
	private String requestType;
	private String requestTypeNm;
	private String requestNo;
	private String requestOrgNo;
	private String peerReviewNo;
	private String peerreviewNo;
	private String objectNo;
	private String title;
	private String peerReviewNm;
	private String peerreviewNm;
	private String objectNm;
	private String peerReviewTitle;
	private String peerreviewTitle;
	private String statusCd;
	private String statusNm;
	private String status;
	private String requestDt;
	private String insertDt;
	private String reviewdate;
	private String reviewDate;
	private String insertUid;
	private String insertUserNm;
	private String approver;
	private String reviewerUser;
	private String approvedUsers;
	private String processingStatus;
	private String objectId;
	private String revNo;

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}

	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}

	public String getReviewdate() {
		return normalizeDate(reviewdate);
	}

	public void setReviewdate(String reviewdate) {
		this.reviewdate = reviewdate;
		this.reviewDate = reviewdate;
	}

	public String getReviewDate() {
		if (reviewDate != null && !reviewDate.isEmpty()) {
			return normalizeDate(reviewDate);
		}
		return getReviewdate();
	}

	public void setReviewDate(String reviewDate) {
		this.reviewDate = reviewDate;
		if (this.reviewdate == null || this.reviewdate.isEmpty()) {
			this.reviewdate = reviewDate;
		}
	}

	private String normalizeDate(String value) {
		if (value == null) {
			return "";
		}
		String date = value.trim();
		if (date.length() >= 10) {
			return date.substring(0, 10);
		}
		return date;
	}

	public String getInsertUserNm() {
		return insertUserNm;
	}

	public String getReviewerUser() {
		return reviewerUser;
	}

	public String getStatus() {
		return status;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getProcessingStatus() {
		return processingStatus;
	}

	public String getPeerReviewNo() {
		if (peerReviewNo != null && !peerReviewNo.isEmpty()) {
			return peerReviewNo;
		}
		if (peerreviewNo != null && !peerreviewNo.isEmpty()) {
			return peerreviewNo;
		}
		return requestNo;
	}

	public String getPeerReviewNm() {
		if (peerReviewNm != null && !peerReviewNm.isEmpty()) {
			return peerReviewNm;
		}
		if (peerreviewNm != null && !peerreviewNm.isEmpty()) {
			return peerreviewNm;
		}
		return title;
	}

	public String getPeerreview_no() {
		return getPeerReviewNo();
	}

	public String getPeerreview_nm() {
		return getPeerReviewNm();
	}
}
