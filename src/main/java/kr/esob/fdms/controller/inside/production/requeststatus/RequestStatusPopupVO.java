package kr.esob.fdms.controller.inside.production.requeststatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupVO {
	private String requestNo;			// 요청번호
	private String userNm;				// 요청자이름
	private String approvalUserNm;		// 결재자 이름
	private String deployDate;			// 배포일자
	private String deployTerm;			// 배포 기간
	private String deployType;			// 배포 유형
	private String watermarkYn;			// 워터마크 사용 여부
	private String requestReason;		// 요청사유
	private String rejectReason;		// 거절사유
	private String statusNm;			// 결재상태

	//생산기술자료 접수자 정보
	private String deptNm;				// 부서명
	private String acceptUserNm;		// 성명
	private String copy;				// 부수

	//생산기술자료 아이템 정보
	private String productNm;			// 기종명
	private String objectClassNm2;
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String revNo;				// 리비전 번호
	private int deployCount;			// 배포매수
	private int destroyCount;		// 폐기매수
	private int itemCount;			// 매수
	private int totalCount;			// 총 배포 매수
	private String objectId;
	private String objectTypeData;

	private String actionCd;

	private String fileNm;			// 파일명
	private int fileNo;				// 파일 순서
}