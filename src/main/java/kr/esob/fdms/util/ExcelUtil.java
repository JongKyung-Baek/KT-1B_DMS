package kr.esob.fdms.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import jxl.Cell;

public class ExcelUtil {
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_LEFT = 2;
	public static final int ALIGN_RIGHT = 3;

	/**
	 * 합계 영역의 height를 구한다.
	 * @return
	 */
	public static short getTotalHeight() {
		return 500;
	}

	/**
	 * Title 영역의 높이를 구한다.
	 * @return
	 */
	public static short getTitleHeight() {
		return 500;
	}

	/**
	 * header 영역의 높이를 구한다.
	 * @return
	 */
	public static short getHeaderHeight() {
		return 400;
	}

	/**
	 * body 영역의 높이를 구한다.
	 * @return
	 */
	public static short getBodyHeight() {
		return 400;
	}

	/**
	 * Excel title 영역의 style
	 * @param wb
	 * @return
	 */
	public static HSSFCellStyle getTitleStyle(HSSFWorkbook wb) {
    	//제목 폰트
        HSSFFont hf = wb.createFont();

        hf.setFontHeightInPoints((short) 9);
        hf.setColor((short) HSSFColor.BLACK.index);
        hf.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        //Header style setting
        HSSFCellStyle hcs = wb.createCellStyle();
        hcs.setFont(hf);
        hcs.setAlignment(HSSFCellStyle.ALIGN_CENTER);


        //set border style
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THICK);
        hcs.setBorderRight(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderTop(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        //set color
        hcs.setFillBackgroundColor((short) HSSFColor.WHITE.index );
        hcs.setFillForegroundColor((short) HSSFColor.WHITE.index );
        hcs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        hcs.setLocked(true);
        hcs.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        return hcs;
    }

	/**
	 * 엑셀에서 header영역의 style을 만든다.
	 * @param wb
	 * @return
	 */
	public static HSSFCellStyle getHeaderStyle(HSSFWorkbook wb) {
    	//제목 폰트
        HSSFFont hf = wb.createFont();

        hf.setFontHeightInPoints((short) 10);
        hf.setColor((short) HSSFColor.WHITE.index);
        hf.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        //Header style setting
        HSSFCellStyle hcs = wb.createCellStyle();
        hcs.setFont(hf);
        hcs.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        //set border style
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THICK);
        hcs.setBorderRight(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderTop(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        //set color
        hcs.setFillBackgroundColor((short) HSSFColor.WHITE.index );
        hcs.setFillForegroundColor((short) HSSFColor.VIOLET.index );
        hcs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        hcs.setLocked(true);
        hcs.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        return hcs;
    }

	/**
	 * 엑셀 다운로드 body 영역 style
	 * @param wb
	 * @return
	 */
	public static HSSFCellStyle getBodyStyle(HSSFWorkbook wb) {
    	//제목 폰트
        HSSFFont hf = wb.createFont();

        hf.setFontHeightInPoints((short) 10);
//        hf.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        //Header style setting
        HSSFCellStyle hcs = wb.createCellStyle();
        hcs.setFont(hf);
        hcs.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        //set border style
//        hcs.setBorderBottom(HSSFCellStyle.BORDER_THICK);
//        hcs.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        hcs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        hcs.setBorderTop(HSSFCellStyle.BORDER_THIN);
//        hcs.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        //set color
        hcs.setLocked(true);
        hcs.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        return hcs;
    }

	/**
	 * Excel 합계 영역의 style
	 * @param wb
	 * @return
	 */
	public static HSSFCellStyle getTotalStyle(HSSFWorkbook wb) {
		//제목 폰트
        HSSFFont hf = wb.createFont();

        hf.setFontHeightInPoints((short) 10);
        hf.setColor((short) HSSFColor.WHITE.index);
        hf.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        //Header style setting
        HSSFCellStyle hcs = wb.createCellStyle();
        hcs.setFont(hf);
        hcs.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        //set border style
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THICK);
        hcs.setBorderRight(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderTop(HSSFCellStyle.BORDER_THIN);
        hcs.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        //set color
        hcs.setFillBackgroundColor((short) HSSFColor.WHITE.index );
        hcs.setFillForegroundColor((short) HSSFColor.VIOLET.index );
        hcs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        hcs.setLocked(true);
        hcs.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        return hcs;
    }

	public static String getString(Cell cell) {
		String cellData	= cell.getContents();
		if (cellData == null) return null;
		return String.valueOf(cellData);
	}

	public static int getInt(Cell cell) {
		String cellData	= getString(cell).trim();
		if (cellData == null || cellData.isEmpty()) return 0;
		return Integer.valueOf(	cellData);
	}

	public static int getDepth(String str) {
		int depth = 0;
		for (int i=0; i < str.length(); i++) {
			if (str.charAt(i) != ' ') break;
			depth++;
		}
		return depth / 2;
	}

	public static String getMenuCode(int v) {
		System.out.println(v);
		System.out.println(String.format("%03d", v));
		return "MENU_" + String.format("%03d", v);
	}

//	public static void main(String [] args) {
//		String a = "|PC|PSS|EG|";
//
//		String []b = a.split("[|]");
//
//		System.out.println(b.length);
//
//		for(int i=0; i<b.length; i++) {
//
//			System.out.println(b[i].isEmpty());
//
//			System.out.println(b[i]);
//		}
//	}

}
