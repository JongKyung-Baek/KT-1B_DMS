package kr.esob.fdms.controller.inside.distribution.swrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.outside.sw.request.SwInfoVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SwRequestParam extends CommonParam {
	private String objectId;
	private String businessTypeCd;
	private String distributeTypeCd;
	private String swNoPrefix;
	private String swTreeCd;
	private String swNo;
	private String swNm;
	private String fileViewNm;
	private String fileNm;
	private String swTypeCd;
	private String insertUid;
	private String ecnNo;
	private String companyCd;
	private String purchaserUserCd;
	private String productNo;
	private String businessAreaCd;
	private String insertStartDt;
	private String insertEndDt;
	private String interfaceStartDt;
	private String interfaceEndDt;
	private String productCd;
	private String [] specialCondition;
	private String searchAllParam;
	private List<SwInfoVO> swList;
	private String useLike;
	private String validType;
	private String createStartDt;
	private String createEndDt;

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

	public List<String> getSwTreeCdList() {
		List<String> result = new ArrayList<>();
		if (swTreeCd == null || swTreeCd.trim().isEmpty()) {
			return result;
		}
		for (String token : swTreeCd.split(",")) {
			if (token != null && !token.trim().isEmpty()) {
				result.add(token.trim());
			}
		}
		return result;
	}

}
