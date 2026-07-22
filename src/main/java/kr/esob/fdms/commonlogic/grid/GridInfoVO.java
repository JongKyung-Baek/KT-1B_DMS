package kr.esob.fdms.commonlogic.grid;

import java.util.HashMap;
import java.util.Map;

import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GridInfoVO {
	private String label;
	private String columnId;
	private String name;
	private int width;
	private String align;
	private StatusYn sortable = StatusYn.N;
	private String formatter = "";
	private StatusYn hidden = StatusYn.N;
	private StatusYn frozen = StatusYn.N;
	private StatusYn editable = StatusYn.N;
	private int maxlength;
	private String sortColumnNm;

	public boolean getSortable() {
		return sortable.isBooleanValue();
	}

	public boolean getHidden() {
		return hidden.isBooleanValue();
	}

	public boolean getFrozen() {
		return frozen.isBooleanValue();
	}

	public String getFormatter() {
		if(!"".equals(formatter)) {
			return "#NO_QUOT#" + formatter + "#NO_QUOT#";
		}
		return formatter;
	}

	public boolean getEditable() {
		return editable.isBooleanValue();
	}

	public Map<String, Object> getEditoptions(){
		if(!editable.isBooleanValue())return null;
		Map<String, Object> editoptions = new HashMap<String, Object>();
		editoptions.put("maxlength", maxlength);
		return editoptions;
	}

}
