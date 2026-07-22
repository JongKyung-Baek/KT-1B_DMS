package kr.esob.fdms.commonlogic.mail;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class MailInfoVO {
	private String requestType;
	private String status;
	private String approvalGrade;
	private Long processSeq;
	private String requestNo;
	private String fromMail;
	private String toMail;
	private String fromUserId;
	private String fromInOut;
	private String toUserId;
	private String bizNo;
	private List<String> toCc;
	private String title;
	private String content;
	private String appendContent;
	private String result;	// 메일 발송 결과
	private String detail;
	private String drawingNo;
	private String drawingNm;
	private String isNewRevision;
	private String mailId;
	private String referenceKeys;

	public String getAppendContent() {
		if(null == appendContent) { return ""; }

		return appendContent;
	}

	private DocsMailEnum mailEnum;

}
