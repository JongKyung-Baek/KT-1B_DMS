package kr.esob.fdms.commonlogic.value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import kr.esob.fdms.commonlogic.combo.ComboCdVO;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfigVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Scope(value= WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component("session")
public class SessionValue implements Serializable{
	private static final long serialVersionUID = 7508111457112535143L;
	private List<MenuVO> menuTop;
	private List<MenuVO> menuSub;
	public String urlType;
	private String sessionLang;
	private String timeoutSecond;
	private Map<String, String> comboLang;
	private Map<String, String> systemMap;

	public String getComboLang(String comboCd, String value) {
		return comboLang.get(comboCd + "|" + value);
	}
}
