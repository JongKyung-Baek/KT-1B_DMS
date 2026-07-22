package kr.esob.fdms.controller.inside.distribution.docpdfrequest;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.inside.distribution.docpdfrequest.DocPdfRequestVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocPdfRequestParam extends CommonParam {
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
	private String searchAllParam;
	private List<DocPdfRequestVO> documentList;
	private String useLike;
	private String validType;
	
	private String btnConvert;
	private String btnViewer;
	private String btnConvertViewer;
}
