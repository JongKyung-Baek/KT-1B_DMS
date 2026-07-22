package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsListVO {
	private String managementNo;                            /* 관리번호 */
	private String businessAreaCd;                          /* 사업장 */
	private String vendorNm;                                /* 업체명 */
	private String vendorUsernm;                            /* 업체담당자 */
	private String createDate;                              /* 등록일자 */
	private String defensePurNm;                            /* 디펜스 구매기획팀 담당자 */
	private String defensePurAcceptYn;                      /* 구매기획팀 접수여부*/
	private String duanzongDt;                              /* 단종시스템반영일자 */
	
	public String getCreateDate() {
		return DateUtil.getYmd(createDate);
	}
	
	public String getDuanzongDt() {
		return DateUtil.getYmd(duanzongDt);
	}
}
