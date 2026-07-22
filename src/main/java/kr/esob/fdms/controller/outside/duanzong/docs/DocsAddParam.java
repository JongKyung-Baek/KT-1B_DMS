package kr.esob.fdms.controller.outside.duanzong.docs;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsAddParam extends CommonParam {
	private String managementNo;
	private String businessAreaCd;
	private String vendorCd;
	private String vendorNm;
	private String vendorUserId;
	private String vendorUserNm;
	private String defensePurId;
	private String defensePurNm;

	private String fileNm;
	private String filePath;
	private String filePathE;
}

