package kr.esob.fdms.controller.outside.commondestroystatus;

import java.util.List;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyStatusInfoVO {
	private String destroyRequestNo;
	private String destroyNo;
	private String requestDesc;
	private String destroyStatusCd;
	private String destroyType;

	private List<DestroyFileVO> fileList;

	public String getDestroyType() {
		return ComboLang.getComboLang("destroyType", this.destroyType);
	}



}
