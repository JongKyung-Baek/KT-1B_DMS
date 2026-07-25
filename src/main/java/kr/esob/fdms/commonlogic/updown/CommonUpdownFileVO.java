package kr.esob.fdms.commonlogic.updown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonUpdownFileVO extends ResultVO{
	private String fileSeq;
	private String fileNm;
	private String orgFileNm;
	private String fileExt;
	private String fileSize;
	private String filePathNm;
	private String endDate;

	private String dataNo;
	private String dataSeq;
	private String docSeq;
	private String objectId;
	private String objectType;
	private String fileNo;
	private String objectNo;
	private String requestNo;
	private String folderName;

	/** Internal storage location. Never serialize it to a client. */
	@JsonIgnore
	public String getFilePathNm() {
		return filePathNm;
	}
	
	public String getEndDate() {
		String rtn = endDate;
		String from = this.endDate;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date dateFrom = null;
		try {
			dateFrom = format.parse(from);
			format = new SimpleDateFormat("yyyy-MM-dd");
			rtn = format.format(dateFrom);
		} catch (ParseException e) {
			rtn = this.endDate;
		}
		return rtn;
	}
}
