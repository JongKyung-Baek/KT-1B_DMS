package kr.esob.fdms.commonlogic.excel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalParam {
	private List<ColModel> colModels;
	private String gridId;
	private List<ListParam> list;
}
