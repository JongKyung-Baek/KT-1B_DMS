package kr.esob.fdms.util;

import java.io.File;

public class StringUtil {
    /**
     * 사업자번호를 구한다. 1234567890 -> 123-45-67890
     * @param v
     * @return
     */
    public static String getBizNo(String v) {
        if(null == v) {
            return "";
        }
        if(v.length() != 10) {
            return v;
        }

        return v.substring(0,3) + "-" + v.substring(3,5) + "-" + v.substring(5,10);
    }

    /**
     * XSS 방지를 위한 HTML 변환
     * @param s
     * @return
     */
    public static String encodeHTML(String s) {
    	s = s.replace("'", "\'");
    	//s = s.replace("&", "&amp;");
    	s = s.replace("<", "&lt;");
    	s = s.replace(">", "&gt;");

    	return s;
    }
    
    public static String replaceLfiPath(String path) {
    	path = path.replace("/", File.separator);
		path = path.replace("\\", File.separator);
		
		String splitPath[] = path.split("\\"+File.separator);
		String fileName = splitPath[splitPath.length-1];
		StringBuffer sb = new StringBuffer();

		for(int i=0; i<splitPath.length-1; i++ ) {
			if(splitPath[i].contains(".")) {
				String strPath = splitPath[i].replaceAll("\\.", "");
				
				sb.append(strPath);
				if(!"".equals(strPath)) {
					sb.append(File.separator);
				}
				
			} else {
				sb.append(splitPath[i]);
				sb.append(File.separator);
			}
		}
		sb.append(fileName);
		path = sb.toString();
    	return path;
    }
}
