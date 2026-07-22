package kr.esob.fdms.controller.configurationmanage.notice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigNoticeListVO {
	private Long noticeCd;						/* 공지코드 */
	private String insertDt; 					/* 등록일자 */
	private String title; 						/* 제목 */
	private String insertUserNm; 				/* 게시자 */
	private String noticeType; 					/* 유형 */
	private Integer noticeHit; 					/* 조회 */
}
