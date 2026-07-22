package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionStatusListParam extends CommonParam{
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String objectType;			// 자료유형
	private String businessAreaCd;		// 사업장
	private String insertUid;			// 등록자
	private String insertDeptNm;		// 등록팀
	private String objectClassCd2;		// 자료구분
	private String deptNm;				// 접수직
	private String updateStartDt;
	private String updateEndDt;
	private List<ProductionInfoVO> productionList;
	private String [] specialCondition;	// 최종 revision

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
