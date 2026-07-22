package kr.esob.fdms.commonlogic.combo;

import java.util.Map;

public class ComboLang {
	public static Map<String, String> comboLang;
    public static String getComboLang(String comboCd, String value) {
    	if(!comboLang.isEmpty() && comboLang.containsKey(comboCd + "|" + value)) {
    		return comboLang.get(comboCd + "|" + value);
    	}else {
    		return value;
    	}
    }

}
