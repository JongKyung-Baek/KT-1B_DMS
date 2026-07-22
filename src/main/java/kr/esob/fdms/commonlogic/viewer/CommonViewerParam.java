package kr.esob.fdms.commonlogic.viewer;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.login.UserVO;
import lombok.*;

@SuppressWarnings("serial")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonViewerParam extends CommonParam{
	private String objectId;
	private String objectType;
	private String requestNo;
	private String requestType;
	private String fileNo;
	private String actionCd;


	//워터마크
	private String userType;
	private String watermarkType;
	private String endDate;
	private String protectYn;

	private String msgCode;

	List<CommonViewerParam> list;

	@Override
	public String toString() {
		return "CommonViewerParam{" +
				"objectId='" + objectId + '\'' +
				", objectType='" + objectType + '\'' +
				", requestNo='" + requestNo + '\'' +
				", requestType='" + requestType + '\'' +
				", fileNo='" + fileNo + '\'' +
				", actionCd='" + actionCd + '\'' +
				", userType='" + userType + '\'' +
				", watermarkType='" + watermarkType + '\'' +
				", endDate='" + endDate + '\'' +
				", protectYn='" + protectYn + '\'' +
				", msgCode='" + msgCode + '\'' +
				", list=" + list +
				'}';
	}
}
