package kr.esob.fdms.controller.outside.cr.request;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrRequestParam extends CommonParam {
	private int cnSerial;
	private String requestNo;
	private String crNo;
	private String businessAreaCd;
	private String businessTypeCd;
	private String productNo;
	private String productCd;
	private String drawingNo;
	private String revNo;
	private String drawingNm;
	private String crTitleNm;
	private String materialNo;
	private String partNo;
	private String purchaserUid;
	private String purchaserUserCd;
	private String purchaserTlUid;
	private String crTypeCd;
	private Date drawingInsertDt;
	private String asIsDesc;
	private String toBeDesc;
	private String vendorNm;
	private String vendorNo;
	private String vendorUid;
	private String vendorUidNm;
	private String vendorUserCd;
	private String vendorEmailNm;
	private int statusCd;
	private Date receiptDt;
	private String insertUid;
	private Date insertDt;
	private String errcod;
	private String errmsg;
	private String filePathNm;
	private String orgFileNm;
	private int fileNo;
	private String fileNm;
	private Long fileSize;
	private String approvalUser;
	private String deviceNm;

	private String objectId;
	private List<MultipartFile> fileList;

}
