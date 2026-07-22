package kr.esob.fdms.controller.configurationmanage.notice;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigNoticeListParam extends CommonParam {
	private String noticeType; 					/* 유형 */		
	private String insertUserNm; 				/* 게시자 */
	private String title; 						/* 제목 */
	private String insertStartDt;				/* 등록일 시작*/
	private String insertEndDt;					/* 등록일 끝*/
}
