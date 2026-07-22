package kr.esob.fdms.controller.inside.distribution.dxf;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.controller.outside.dxf.request.DxfInfoVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DxfRequestParam extends CommonParam {
	private String objectId;
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String objectType;			// 자료유형
	private String businessAreaCd;		// 사업장
	private String insertUid;			// 등록자
	private String insertDeptNm;		// 등록팀
	private String objectClassCd2;		// 자료구분
	private String insertStartDt;
	private String insertEndDt;
	private String interfaceStartDt;
	private String interfaceEndDt;
	private String searchAllParam;
	private String useLike;
	private List<DxfInfoVO> dxfList;
	private String [] specialCondition;	// 최종 revision
	private String createStartDt;
	private String createEndDt;
	private String deptNm;
	private String treeCd;
	private String drawingNo;


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
