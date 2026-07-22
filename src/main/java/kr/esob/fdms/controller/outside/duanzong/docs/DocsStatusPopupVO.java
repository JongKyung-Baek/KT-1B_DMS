package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsStatusPopupVO extends CommonParam {
	private String businessAreaCd;                          /* 사업장 */
	private String vendorNm;                                /* 업체명 */
	private String vendorUsernm;                            /* 업체담당자 */
	private String defensePurNm;                            /* 디펜스 구매기획팀 담당자 */
	private String managementNo;							/* 관리번호 */
	private String fileNm;									/* 파일 이름*/
	private String filePath;								/* 내부 파일 경로*/
	private String filePathE;								/* 외부 파일 경로*/
}
