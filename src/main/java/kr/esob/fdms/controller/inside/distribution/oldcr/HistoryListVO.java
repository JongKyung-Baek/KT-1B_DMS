package kr.esob.fdms.controller.inside.distribution.oldcr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String objectId	;		/* 제의번호 */
	private String cnDrawNumber;		/* 도번 */
	private String fileName	;		/* 도명 */
	private String revision;			/* REV */
	private String cnTitle;			/* 제목 */
	private String firstName;		/* 구매담당자 */
	private String creationDate;		/* 요청일 */
	private String approvalDate;		/* 승인일 */
	private String state;			/* 진행상태 */
	private String userName;			/* 업체담당자 */
	private String rejectComment;	/* 요청거부사유 */
	private String cnErpMatNo;		/* 자재코드 */
	private String cnPartNumber; 	/* 규격화품번 */
	private String cnResult;			/* 검토의견 */
	private String usageVal;			/* 제의사유코드*/
	private String outuserId;		/* 업체담당자 ID*/
	private String inUserId;			/* 구매담당자 ID*/
	private String state2;			/* 진행상태 */
}