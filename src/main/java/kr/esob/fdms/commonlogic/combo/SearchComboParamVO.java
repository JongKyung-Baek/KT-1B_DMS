package kr.esob.fdms.commonlogic.combo;


import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchComboParamVO extends CommonParam {
	private Integer posStart;
	private Integer count;
	private String lang;
	private String queryId;
	private String term;
	private String userCd;
	private String companyCd;
	private String deptCd;
	private String businessAreaCd;
	private String defaultText;
}
