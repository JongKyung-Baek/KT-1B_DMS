package kr.esob.fdms.commonlogic.viewer;

import org.apache.commons.io.FilenameUtils;

import kr.esob.fdms.commonlogic.result.ResultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonViewerVO extends ResultVO{
	private String filePath;
	private String fileNm;
	private String fileOrgNm;
	private String failType;
	private String viewerCabUrl;
	private String objectId;
	private String requestNo;
	private String fileNo;
	private String securityTypeCd;
	private int printCount;
	private String requestUserCd;
	private String requestType;
    private String objectType;
	private String authLevel;
	private String destroyStatusCd;		// 폐기상태
	private String destroyType;			// 폐기구분
    //private String extName;			// 확장자

	private String destroyRequestUserNm; // 폐기요청자


    public String getExtName() {
    	return FilenameUtils.getExtension(filePath);
    }

	//워터마크용 데이터
	private String watermarkInfo;
	private String userNm;
	private String printDate;
	private String endDate;
	private String businessTypeNm;
	private String distributeTypeNm;
	private String purchaserAppDate;
	private String requestUserNm;
	private String protectYn;
	private String requestPurpose;
	private String deployDt;
}
