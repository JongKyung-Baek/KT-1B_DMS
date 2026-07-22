package kr.esob.fdms.commonlogic.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultVO {
	private boolean isSuccess;
	private String failReason;
	private Object data;
	private String message;
	private String redirectUrl;
}
