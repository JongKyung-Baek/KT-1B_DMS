package kr.esob.fdms.controller.inside.distribution.history;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyPopupParam extends CommonParam {
	private String destroyType;		// 폐기 유형
	private String destroyDesc;		// 폐기 사유
	private String destroyNo;		// 폐기 번호
	private int destroyFileSeq;		// 파일 순번
	private String filePath;		// 파일 경로
	private String destroyRequestNo;
	private String requestType;
	private String businessAreaCd;

	private String approvalLineId;
	private String approvalUser;

	private String requestNo;		// 요청번호
	private String objectId;		// 아이템 번호
	private String objectNo;
	private int fileNo;
	
	private String fileNm;
	private String filePathInside; // 내부사용자 파일경로

	private StatusYn sendEmailYn = StatusYn.Y;
	
	List<DestroyPopupParam> list;
}
