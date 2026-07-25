package kr.esob.fdms.controller.outside.cr.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutsideCrParam extends CommonParam {
	private String crNo;
	@JsonIgnore
	private String requestNo;
	private String approvalUser;
	private String reviewResult;
	/**
	 * CR상태
	 * 1: CR요청(협력사)
	 * 2: 구매담당자 거절
	 * 3: 구매담당자 접수
	 * 4: 구매팀장 거절
	 * 5: 구매팀장 승인
	 * 6: PDM 전송(접수)
	 */
	@JsonIgnore
	private int statusCd;
	@JsonIgnore
	private String reqStatusCd;
	private String materialNo;
	private String productNo;

	@JsonIgnore
	private String actionCd;
	@JsonIgnore
	private String approvalGradeCd;
	@JsonIgnore
	private String requestDesc;
	@JsonIgnore
	private String actualUserCd;
	@JsonIgnore
	private String approvalStatusCd;
	private String rejectDesc;
	@JsonIgnore
	private int currentProcessSeqNo;
	@JsonIgnore
	private List<String> filePathNmList;


}
