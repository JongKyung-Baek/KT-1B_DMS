package kr.esob.fdms.commonlogic.down;

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
public class CommonDownFileVO extends ResultVO{
	private String fileNm;
	private String orgFileNm;
	private String fileSize;
	private String filePathNm;

	@JsonIgnore
	public String getFilePathNm() {
		return filePathNm;
	}

}
