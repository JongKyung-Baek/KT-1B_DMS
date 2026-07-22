package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocPdfLinkRequestParam extends CommonParam {
	private String filePathNm;
	private String orgFileNm;
	private String FileNm;
	private String viewFilePath;
	
}
