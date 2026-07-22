package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintDestroyItemListVO {
	private String destroyRequestNo; 			// 폐기 요청 번호
	private String objectId;					// 아이템 번호
	private String requestNo;					// 요청 번호
	
	private String requestDesc;					// 요청 사유
	private String objectType;					// 자료 유형
}