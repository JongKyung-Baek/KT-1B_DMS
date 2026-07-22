package kr.esob.fdms.controller.inside.distribution.docpdfrequest;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocPdfRequestVO {
	private String seqNo;
	private String cnSerial;
	private String objectId;
	private int fileNo;
	private String filePathNm;
	private String orgFileNm;
	private String cvrtFileNm;
	private Integer currentPageNo;
	private Integer totalPageNo;
	private String fileNm;
	private String DistibuteTypeCd;
	private String filesize;
	private String interfaceDt;
	private String btnConvert;
	private String btnViewer;
	private String btnConvertViewer;

	
	/*
	public String getStdGappDt() {
		return DateUtil.getYmd(stdGappDt);
	}
	public String getChangeGappDt() {
		return DateUtil.getYmd(changeGappDt);
	}
	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}
	public String getUpdateDt() {
		return DateUtil.getYmd(updateDt);
	}
	public String getInterfaceDt() {
		return DateUtil.getYmd(interfaceDt);
	}
	*/
}
