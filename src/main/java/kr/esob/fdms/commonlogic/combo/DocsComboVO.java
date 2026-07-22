package kr.esob.fdms.commonlogic.combo;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocsComboVO {
	private Map<String, List<ComboCdVO>> comboCd;

}
