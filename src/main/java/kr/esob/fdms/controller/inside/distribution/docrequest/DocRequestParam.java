package kr.esob.fdms.controller.inside.distribution.docrequest;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.outside.doc.request.DocInfoVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocRequestParam extends CommonParam {
	private String businessTypeCd;
	private String distributeTypeCd;
	private String distributeTypeNm;
	private String docTreeCd;
	private String documentNoPrefix;
	private String documentNo;
	private String objectId;
	private String documentNm;
	private String docClassCd2;
	private String docClassNm2;
	private String insertUid;
	private String ecnNo;
	private String purchaserUserCd;
	private String businessAreaCd;
	private String insertStartDt;
	private String insertEndDt;
	private String interfaceStartDt;
	private String interfaceEndDt;
	private String productCd;
	private String [] specialCondition;
	private String searchAllParam;
	private List<DocInfoVO> documentList;
	private String useLike;
	private String validType;

	public String getVendorYn() {
		if(null == specialCondition) {
			return "N";
		}
		for(int i=0; i<specialCondition.length; i++) {
			if(specialCondition[i].equals("vendor")) {
				return "Y";
			}
		}

		return "N";
	}

	public String getLastRevisionYn() {
		if(null == specialCondition) {
			return "N";
		}
		for(int i=0; i<specialCondition.length; i++) {
			if(specialCondition[i].equals("lastRevision")) {
				return "Y";
			}
		}

		return "N";
	}
}
