/**
 * Esob Document External
 *
 * (Copyright ⓒ 2023 Esob Co., Ltd. All Rights Reserved)
 *
 * $Revision: 414 $
 * $LastChangedDate: 2023-03-19 09:50:35 +0900 (수, 19 3 2023) $
 * $LastChangedBy: younjh $
 */
package kr.esob.fdms.commonlogic.excel;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.grid.GridInfoVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.RootAbsolutePath;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.RequestUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @classname : CreateExcelService
 * @author    : InHyeri
 * @date      : 2018. 8. 27. 오전 9:56:27
 * @desc      :
 */
@Service
public class CreateExcelService {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private CreateExcelDao dao;

	@Inject
	private RootAbsolutePath rootAbsolutePath;

	@Inject
	protected RequestUtil requeststil;

	public CreateExcelVO createExcel2(HttpServletRequest request) throws Exception {
		CreateExcelVO createExcelVo = new CreateExcelVO();
		//		paramMap.put("GRID_ID", request.getParameter("GRID_ID"));
		//		paramMap.put("LIST_ID", request.getParameter("LIST_ID"));
		Map<String, String> paramMap = requeststil.getRequestParameterToMap(request);

		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if("gridOutDistributionOldHistoryList".equals(paramMap.get("gridId"))) {
			paramMap.put("companyCode", user.getCompanyCd());
		}

		int totalCount = checkExcelCount(paramMap);

		String excelFilePath = getExcelFileName(rootAbsolutePath.toString());

		makeExcel(paramMap, excelFilePath, totalCount);

		createExcelVo.setResult(Constant.RESULT_SUCCESS);
		createExcelVo.setUrl(request.getContextPath() + "/resources/" + excelFilePath);

		return createExcelVo;
	}


	public CreateExcelVO createExcelFromLocalGrid(HttpServletRequest request, LocalParam param) throws Exception {
		CreateExcelVO createExcelVo = new CreateExcelVO();
		//Map<String, String> paramMap = requeststil.getRequestParameterToMap(request);
		String excelFilePath = getExcelFileName(rootAbsolutePath.toString());

		makeExcelLocal(param, excelFilePath);

		createExcelVo.setResult(Constant.RESULT_SUCCESS);
		createExcelVo.setUrl(request.getContextPath() + "/resources/" + excelFilePath);

		return createExcelVo;
	}

	public CreateExcelVO createExcel(HttpServletRequest request) throws Exception {
		CreateExcelVO createExcelVo = new CreateExcelVO();
		//		paramMap.put("GRID_ID", request.getParameter("GRID_ID"));
		//		paramMap.put("LIST_ID", request.getParameter("LIST_ID"));
		Map<String, String> paramMap = requeststil.getRequestParameterToMap(request);

		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if("gridOutDistributionOldHistoryList".equals(paramMap.get("gridId"))) {
			paramMap.put("companyCode", user.getCompanyCd());
		}

		int totalCount = checkExcelCount(paramMap);

		String excelFilePath = getExcelFileName(rootAbsolutePath.toString());

		makeExcel(paramMap, excelFilePath, totalCount);

		createExcelVo.setResult(Constant.RESULT_SUCCESS);
		createExcelVo.setUrl(request.getContextPath() + "/resources/" + excelFilePath);

		return createExcelVo;
	}
	
	private XSSFCellStyle setBackgroundColor(CellStyle cellStyle, XSSFColor xssfColor) {
		XSSFCellStyle style = (XSSFCellStyle) cellStyle;
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(xssfColor);
		return style;
	}
	
	public CreateExcelVO createExcelDuanzongPdm(HttpServletRequest request, CommonParam param) throws Exception {
		CreateExcelVO createExcelVo = new CreateExcelVO();
		Map<String, String> paramMap = requeststil.getRequestParameterToMap(request);
		
		paramMap.put("delivery_vendor_nm", param.getSessionUser().getCompanyNm());
		int totalCount = checkExcelCount(paramMap);
		String excelFilePath = getExcelFileName(rootAbsolutePath.toString());

		makeCreateExcelDuanzongPdm(paramMap, excelFilePath, totalCount, param);
		
		createExcelVo.setResult(Constant.RESULT_SUCCESS);
		createExcelVo.setUrl(request.getContextPath() + "/resources/" + excelFilePath);
		
		return createExcelVo;
	}

	private int checkExcelCount(Map<String, String> paramMap) throws Exception{
		String totalCountQueryId = paramMap.get("listId").toString() + "Count";
		int totalCount = dao.selectCountForExcel(totalCountQueryId, paramMap);
		if(totalCount >= 1000000)throw new Exception("resultCode.00009"); //엑셀 다운로드 가능한 데이터 수를 초과 했습니다.
		return totalCount;
	}

	private String getExcelFileName(String rootAbsolutePath) throws FileNotFoundException{
		Calendar calendar = Calendar.getInstance();
		String yyyymmdd = ""+ calendar.get(Calendar.YEAR)
		+ (calendar.get(Calendar.MONTH) + 1)
		+ calendar.get(Calendar.DAY_OF_MONTH)
		+ calendar.get(Calendar.HOUR_OF_DAY)
		+ calendar.get(Calendar.MINUTE)
		+ calendar.get(Calendar.MILLISECOND)
		;
		File folderPath = new File(rootAbsolutePath+"/excel");
		if(!folderPath.exists()) folderPath.mkdirs(); //excel 폴더가 존재하는지 체크, 없을경우 폴더 생성

		String filePath = "excel/grid_"+yyyymmdd+".xlsx";
		return filePath;
	}
	
	@SuppressWarnings({ "unchecked" })
	public void makeCreateExcelDuanzongPdm(Map<String, String> paramMap, String excelFilePath, int totalCount, CommonParam param) throws Exception{
		// 엑셀파일을 생성할 경로 지정
		System.out.println("excel export 경로 : " + rootAbsolutePath.toString() + excelFilePath);
		FileOutputStream out = new FileOutputStream(rootAbsolutePath.toString() + excelFilePath);
		List<GridInfoVO> columns = new ArrayList<GridInfoVO>();

		// Excel을 그리기 위해 Grid의 정보를 구함.
		columns = dao.selectGridDuanzongPdmInfo(paramMap);

		SXSSFWorkbook wb = new SXSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;
		
		CellStyle labelstyle = null;
		CellStyle datastyle = null;
		Map<String, CellStyle> cellStyle = new HashMap<String, CellStyle>();
		String[] labelArray = {"NO", "최종등록(갱신)일자", "기본정보 (체계기술부서)","BOM Intelligence 획득 데이터 (형상관리 부서)",
       			"기술변경/단종여부 (형상관리부서)", "협력업체 단종정보 획득 (구매기획 부서)", "동등품 (시스템 처리)", "등록정보"};
		int[] labelIndex = {0, 0, 13, 14, 1, 10, 4, 2};
		int labelRowStart = 0, labelRowEnd = 0;
		
		// *** START TITLE ROW
		int colidx = 0, rowidx = 1;
		r = s.createRow(0);
		r.setHeight((short)0x1cc);
		
		XSSFColor color = new XSSFColor(new Color(255, 255, 255));
		
		for (int i = 0; i < labelArray.length; i++) {
			if(i == 1 || i == 3 || i == 4) color = new XSSFColor(new Color(255, 242, 204));
			if(i == 2) color = new XSSFColor(new Color(222, 235, 247));
			else if(i == 5) color = new XSSFColor(new Color(197, 224, 180));
			else if(i == 6) color = new XSSFColor(new Color(231, 230, 230));
			else if(i == 7) color = new XSSFColor(new Color(255, 255, 255));
			labelstyle = setBackgroundColor(getTitleStyle(wb), color);
			labelRowEnd += labelIndex[i];
			s.setColumnWidth(labelRowStart, 400);
			c = r.createCell(labelRowStart, Cell.CELL_TYPE_STRING);
			c.setCellValue(labelArray[i]);			
			c.setCellStyle(labelstyle);
			if(i >= 2) s.addMergedRegion(new CellRangeAddress(0, 0, labelRowStart, labelRowEnd));
			labelRowStart = ++labelRowEnd;
		}
		
		for(int i = 0; i < columns.size(); i++) {
			if(i == 0) color = new XSSFColor(new Color(255, 255, 255));
			else if(i == 1 || i > 16 && i <= 32) color = new XSSFColor(new Color(255, 242, 204));
			else if(i >= 2 && i <= 15) color = new XSSFColor(new Color(222, 235, 247));
			else if(i >= 33 && i <= 43) color = new XSSFColor(new Color(197, 224, 180));
			else if(i >= 44 && i <= 48) color = new XSSFColor(new Color(231, 230, 230));
			else if(i >= 49 && i <= 51) color = new XSSFColor(new Color(255, 255, 255));
			
			if(i == 16 || i == 31) color = new XSSFColor(new Color(255, 255, 0));
			
			if(i == 0) {
				r = s.createRow(rowidx);
				r.setHeight((short)0x1cc);
			}
			
			GridInfoVO col = columns.get(i);
			datastyle = getDataStyle(wb, col);
			if(i == 15) {
				datastyle.setFont(getFont("HIGHLIGHT", wb));
			}
			setBackgroundColor(datastyle, color);
			cellStyle.put(col.getColumnId(), datastyle);
			
			String label = col.getLabel().replace("<br/>", "\n").replace("</br>", "\n");
			int size = col.getWidth();
			s.setColumnWidth(colidx, size*40);
			c = r.createCell(colidx++, Cell.CELL_TYPE_STRING);
			
			c.setCellValue(label);
			CellStyle style = setBackgroundColor(getTitleStyle(wb), color);
			style.setWrapText(true);
			if(i == 10 || i == 11 || i == 16 || i == 40 || i == 41 || i == 42) {
				style.setFont(getFont("HIGHLIGHT", wb));
			}
			c.setCellStyle(style);
		}
		// *** END TITLE ROW
		s.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
		s.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
		

		int pagePerCount = 10000;
		int totalPage = (int)Math.ceil((float) totalCount / pagePerCount);
		// *** START BODY ROWS
		for (int currPage = 1; currPage <= totalPage; currPage++) {
			paramMap.put("page", String.valueOf(currPage));
			paramMap.put("posStart", String.valueOf(((currPage * 10000) - pagePerCount) + 1));
			paramMap.put("count", String.valueOf(pagePerCount));
			
			// 실제 리스트 쿼리 실행
			List<Object> datas = dao.selectListForExcel(paramMap);
			
			for (int i = 0; i < datas.size(); i++) {
				Object data = datas.get(i);
				Method[] methods = data.getClass().getDeclaredMethods();
				r = s.createRow(++rowidx);
				r.setHeight((short)0x190);

				for (int j = 0; j < columns.size(); j++) {
					String key = columns.get(j).getColumnId();
					datastyle = cellStyle.get(key);
					c = r.createCell(j, Cell.CELL_TYPE_STRING);
					String value = "";
					String methodString = key.substring(0,1).toUpperCase()+key.substring(1);

					for (int k= 0; k < methods.length; k++) {
						if (("get"+methodString).equals(methods[k].getName())) {
							value = String.valueOf(methods[k].invoke(data));
							if(null == value || "null".equals(value)) { value = ""; }
							c.setCellValue(value);
							c.setCellStyle(datastyle);
							break;
						}
					}
				}

			}

		}
		// *** END BODY ROWS

		wb.write(out);
		out.close();
		out = null;
	}

	/**
	 * 엑셀 생성 로직
	 * @param columns	: 열 데이터
	 * @param datas		: 로우 데이터
	 * @param out		: 파일생성 객체
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	private void makeExcel(Map<String, String> paramMap, String excelFilePath, int totalCount)throws Exception{
		// 엑셀파일을 생성할 경로 지정
		System.out.println("excel export 경로 : " + rootAbsolutePath.toString() + excelFilePath);
		FileOutputStream out = new FileOutputStream(rootAbsolutePath.toString() + excelFilePath);
		List<GridInfoVO> columns = new ArrayList<GridInfoVO>();

		// Excel을 그리기 위해 Grid의 정보를 구함.
		columns = dao.selectGridInfo(paramMap);
		SXSSFWorkbook wb = new SXSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;

		CellStyle titlestyle = getTitleStyle(wb);
		CellStyle datastyle = null;
		Map<String, CellStyle> cellStyle = new HashMap<String, CellStyle>();
		// *** START TITLE ROW
		r = s.createRow(0);
		r.setHeight((short)0x1cc);
		int colidx = 0, rowidx=1;
		for(GridInfoVO col:columns){
			datastyle = getDataStyle(wb, col);
			cellStyle.put(col.getColumnId(), datastyle);
			String label =col.getLabel().replace("<br/>", "\n").replace("</br>", "\n");
			int size = col.getWidth();
			s.setColumnWidth(colidx, size*40);
			c = r.createCell(colidx++, Cell.CELL_TYPE_STRING);
			c.setCellValue(label);
			titlestyle.setWrapText(true);
			c.setCellStyle(titlestyle);
		}
		// *** END TITLE ROW

		int pagePerCount = 10000;
		int totalPage = (int)Math.ceil((float) totalCount / pagePerCount);

		// *** START BODY ROWS
		for(int currPage=1; currPage <= totalPage ; currPage++) {
			paramMap.put("page", String.valueOf(currPage));
			paramMap.put("posStart", String.valueOf(((currPage * 10000) - pagePerCount) + 1));
			paramMap.put("count", String.valueOf(pagePerCount));

			// 실제 리스트 쿼리 실행
			List<Object> datas = dao.selectListForExcel(paramMap);
			for(int i = 0; i<datas.size(); i++) {
				Object data = datas.get(i);
				Method[] methods = data.getClass().getDeclaredMethods();
				r = s.createRow(rowidx++);
				r.setHeight((short)0x190);

				for(int j = 0; j<columns.size(); j++) {
					String key = columns.get(j).getColumnId();
					c = r.createCell(j, Cell.CELL_TYPE_STRING);
//					for (Field field : data.getClass().getDeclaredFields()){
//						field.setAccessible(true);

						String value = "";

//						System.out.println("key : " + key + ", field.getName() : " + field.getName());

//						if(data.getClass().equals(HashMap.class)) { // 데이터 타입이 vo가 아닌 hashmap인 경우
//							if(key.equals(field.getName()) || data.getClass().equals(HashMap.class)) {
//								Map<String, Object> map = (Map<String, Object>) data;
//								if(null != map.get(key)) value = map.get(key).toString();
//
//								if(null == value) { value = ""; }
//
//								c.setCellValue(value);
//								c.setCellStyle(cellStyle.get(key));
//							}
//						}
//						else {
							String methodString = key.substring(0,1).toUpperCase()+key.substring(1);

							for(int k=0;k<methods.length;k++){
								if(("get"+methodString).equals(methods[k].getName())){
									value = String.valueOf(methods[k].invoke(data));
									System.out.println(" ------- 액셀 몇번째 : " + k +  " --- 데이터 정보 :" + value);
									if(null == value || "null".equals(value)) { value = ""; }
									
									c.setCellValue(value);
									c.setCellStyle(cellStyle.get(key));
									break;
								}
							}
//						}


//						if(key.equals(field.getName()) || data.getClass().equals(HashMap.class)) {
//							c = r.createCell(j, Cell.CELL_TYPE_STRING);
//							String value = "";
//							if(data.getClass().equals(HashMap.class)) { // 데이터 타입이 vo가 아닌 hashmap인 경우
//								Map<String, Object> map = (Map<String, Object>) data;
//								if(null != map.get(key)) value = map.get(key).toString();
//							}else {
//								String methodString = field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
//
//								for(int k=0;k<methods.length;k++){
//									System.out.println("get" + methodString + " : " + methods[k].getName());
//									if(("get"+methodString).equals(methods[k].getName())){
//										value = (String) methods[k].invoke(data);
//										break;
//									}
//								}
//							}
//
//							if(null == value) { value = ""; }
//
//							c.setCellValue(value);
//							c.setCellStyle(cellStyle.get(key));
//						}

//					}

				}

			}

		}

		// *** END BODY ROWS

		//s = wb.createSheet();

		wb.write(out);
		out.close();
		out = null;
	}

	private void makeExcelLocal(LocalParam param, String excelFilePath)throws Exception{
		// 엑셀파일을 생성할 경로 지정
		System.out.println("excel export 경로 : " + rootAbsolutePath.toString() + excelFilePath);
		FileOutputStream out = new FileOutputStream(rootAbsolutePath.toString() + excelFilePath);

//		List<GridInfoVO> columns = new ArrayList<GridInfoVO>();
		List<ColModel> columns = param.getColModels();

		System.out.println(JSONObject.fromObject(param).toString());
		System.out.println(JSONArray.fromObject(columns).toString());

		// Excel을 그리기 위해 Grid의 정보를 구함.
//		columns = dao.selectGridInfo(paramMap);

//		JSONArray columns = JSONArray.fromObject(request.getParameter("colModels"));

		SXSSFWorkbook wb = new SXSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = null;
		Cell c = null;

		CellStyle titlestyle = getTitleStyle(wb);
		CellStyle datastyle = null;
		Map<String, CellStyle> cellStyle = new HashMap<String, CellStyle>();
		// *** START TITLE ROW
		r = s.createRow(0);
		r.setHeight((short)0x1cc);
		int colidx = 0, rowidx=1;
		for(int i=0; i<columns.size(); i++){
			ColModel col = columns.get(i);
			datastyle = getDataStyle(wb, col);
			cellStyle.put(col.getColumnId(), datastyle);
			String label =col.getLabel().replace("<br/>", "\n").replace("</br>", "\n").replace("&lt;/br&gt;", "\n").replace("&lt;br/&gt;", "\n");
			int size = col.getWidth();
			s.setColumnWidth(colidx, size*40);
			c = r.createCell(colidx++, Cell.CELL_TYPE_STRING);
			c.setCellValue(label);
			titlestyle.setWrapText(true);
			c.setCellStyle(titlestyle);
		}
		// *** END TITLE ROW

		int pagePerCount = 10000;
		int currPage = 1;

		// *** START BODY ROWS
//			paramMap.put("page", String.valueOf(currPage));
//			paramMap.put("posStart", String.valueOf(((currPage * 10000) - pagePerCount) + 1));
//			paramMap.put("count", String.valueOf(pagePerCount));

			// 실제 리스트 쿼리 실행
			//List<Object> datas = dao.selectListForExcel(paramMap);
//			List<Map<String, Object>> datas = (List<Map<String, Object>>) paramMap.get("list");

			List<ListParam> datas = param.getList();

			for(int i = 0; i<datas.size(); i++) {
				ListParam data = datas.get(i);

				System.out.println(JSONObject.fromObject(data));

				Method[] methods = data.getClass().getDeclaredMethods();
				r = s.createRow(rowidx++);
				r.setHeight((short)0x190);

				for(int j = 0; j<columns.size(); j++) {
					String key = columns.get(j).getName();

					c = r.createCell(j, Cell.CELL_TYPE_STRING);
//					for (Field field : data.getClass().getDeclaredFields()){
//						field.setAccessible(true);

						String value = "";

//						System.out.println("key : " + key + ", field.getName() : " + field.getName());

//						if(data.getClass().equals(HashMap.class)) { // 데이터 타입이 vo가 아닌 hashmap인 경우
//							if(key.equals(field.getName()) || data.getClass().equals(HashMap.class)) {
//								Map<String, Object> map = (Map<String, Object>) data;
//								if(null != map.get(key)) value = map.get(key).toString();
//
//								if(null == value) { value = ""; }
//
//								c.setCellValue(value);
//								c.setCellStyle(cellStyle.get(key));
//							}
//						}
//						else {
							String methodString = key.substring(0,1).toUpperCase()+key.substring(1);

							for(int k=0;k<methods.length;k++){
								if(("get"+methodString).equals(methods[k].getName())){
									value = String.valueOf(methods[k].invoke(data));

									if(null == value || "null".equals(value)) { value = ""; }

									c.setCellValue(value);
									c.setCellStyle(cellStyle.get(key));
									break;
								}
							}
//						}


//						if(key.equals(field.getName()) || data.getClass().equals(HashMap.class)) {
//							c = r.createCell(j, Cell.CELL_TYPE_STRING);
//							String value = "";
//							if(data.getClass().equals(HashMap.class)) { // 데이터 타입이 vo가 아닌 hashmap인 경우
//								Map<String, Object> map = (Map<String, Object>) data;
//								if(null != map.get(key)) value = map.get(key).toString();
//							}else {
//								String methodString = field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
//
//								for(int k=0;k<methods.length;k++){
//									System.out.println("get" + methodString + " : " + methods[k].getName());
//									if(("get"+methodString).equals(methods[k].getName())){
//										value = (String) methods[k].invoke(data);
//										break;
//									}
//								}
//							}
//
//							if(null == value) { value = ""; }
//
//							c.setCellValue(value);
//							c.setCellStyle(cellStyle.get(key));
//						}

//					}

				}

			}

		// *** END BODY ROWS

		//s = wb.createSheet();

		wb.write(out);
		out.close();
		out = null;
	}

	/**
	 * Title용 스타일
	 * @param wb
	 * @return
	 * @throws Exception
	 */
	private CellStyle getTitleStyle(SXSSFWorkbook wb)throws Exception{
		CellStyle rtn = wb.createCellStyle();
		Font font= getFont("TITLE", wb);
		rtn.setFont(font);
		rtn.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND); // 셀 백 그라운드 채우기 스타일
		rtn.setFillForegroundColor((short) HSSFColor.PALE_BLUE.index);  // 셀 백그라운드 색
		rtn.setAlignment(CellStyle.ALIGN_CENTER); // 가운데 정렬(정렬 선택)
		rtn.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		rtn.setBorderBottom(CellStyle.BORDER_THIN); // 외곽 바닥선 종류 (현재 얅게)
		rtn.setBorderLeft(CellStyle.BORDER_THIN);  // 외곽 왼쪽선 종류
		rtn.setBorderRight(CellStyle.BORDER_THIN); // 외곽 오른쪽 선 종류
		rtn.setBorderTop(CellStyle.BORDER_THIN); // 외곽 위쪽 선 종류

		return rtn;
	}

	/**
	 * DATA용 스타일
	 * @param wb
	 * @return
	 * @throws Exception
	 */
	private CellStyle getDataStyle(SXSSFWorkbook wb, GridInfoVO col)throws Exception{
		CellStyle rtn = wb.createCellStyle();
		String align = col.getAlign();
		Font font= getFont("DATA", wb);
		rtn.setFont(font);
		if("left".equals(align)){
			rtn.setAlignment(CellStyle.ALIGN_LEFT);
		}else if("right".equals(align)){
			rtn.setAlignment(CellStyle.ALIGN_RIGHT);
		}else{
			rtn.setAlignment(CellStyle.ALIGN_CENTER);
		}
		rtn.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		rtn.setBorderBottom(CellStyle.BORDER_THIN);
		rtn.setBorderLeft(CellStyle.BORDER_THIN);
		rtn.setBorderRight(CellStyle.BORDER_THIN);
		rtn.setBorderTop(CellStyle.BORDER_THIN);

		return rtn;

	}

	private CellStyle getDataStyle(SXSSFWorkbook wb, ColModel col)throws Exception{
		CellStyle rtn = wb.createCellStyle();
		String align = col.getAlign();
		Font font= getFont("DATA", wb);
		rtn.setFont(font);
		if("left".equals(align)){
			rtn.setAlignment(CellStyle.ALIGN_LEFT);
		}else if("right".equals(align)){
			rtn.setAlignment(CellStyle.ALIGN_RIGHT);
		}else{
			rtn.setAlignment(CellStyle.ALIGN_CENTER);
		}
		rtn.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		rtn.setBorderBottom(CellStyle.BORDER_THIN);
		rtn.setBorderLeft(CellStyle.BORDER_THIN);
		rtn.setBorderRight(CellStyle.BORDER_THIN);
		rtn.setBorderTop(CellStyle.BORDER_THIN);

		return rtn;

	}

	/**
	 * 폰트
	 * @param type	: TITLE=타이틀용 DATA=데이터용
	 * @param wb
	 * @return
	 * @throws Exception
	 */
	private Font getFont(String type, SXSSFWorkbook wb)throws Exception{
		Font rtn= wb.createFont();
		
		if("HIGHLIGHT".equals(type)){
			rtn.setFontHeightInPoints((short)9); // 글자 크기
			rtn.setColor(HSSFColor.RED.index); // 글자 색
			rtn.setBoldweight(Font.BOLDWEIGHT_BOLD);//  굵은 글자
			rtn.setFontName("Arial");
		} else if("TITLE".equals(type)){
			rtn.setFontHeightInPoints((short)9); // 글자 크기
			rtn.setColor(HSSFColor.BLACK.index); // 글자 색
			rtn.setBoldweight(Font.BOLDWEIGHT_BOLD);//  굵은 글자
			rtn.setFontName("Arial");
		}else{
			rtn.setFontHeightInPoints((short)10);
			rtn.setColor(HSSFColor.BLACK.index);
			rtn.setBoldweight(Font.BOLDWEIGHT_NORMAL); // 평범한 글자
			rtn.setFontName("Arial");
		}

		return rtn;

	}

	//    public CreateExcelVO createExcelDynamicColumn(HttpServletRequest request, List<String> dynamicList, List<String> columnNameList) throws Exception{
	//    	CreateExcelVO createExcelVo = new CreateExcelVO();
	//    	Map<String, Object> paramMap = setParam(request);
	//
	//    	int totalCount = checkExcelCount(paramMap);
	//
	//    	List<GridTagVO> gridList = dao.selectGridInfo(paramMap);
	//
	//    	for(int i=0; i<columnNameList.size(); i++) {
	//    		GridTagVO vo = new GridTagVO();
	//    		vo.setColumnAlign("right");
	//    		vo.setColumnId("col" + i);
	//    		vo.setColumnName(columnNameList.get(i));
	////    		vo.setColumnName(Datestil.getAddDelimiter(dateList.get(i), "-"));
	//    		vo.setColumnSize("100");
	//    		vo.setColumnFormat("int");
	//
	//    		gridList.add(vo);
	//    	}
	//
	//    	paramMap.put("dynamicList", dynamicList);
	//    	paramMap.put("gridList", gridList);
	//
	//    	String excelFilePath = getExcelFileName(rootAbsolutePath.toString());
	//
	//		makeExcel(paramMap, excelFilePath, totalCount, null);
	//
	//		createExcelVo.setResult(ResultVO.RESULT_SUCCESS);
	//		createExcelVo.setUrl(request.getContextPath() + "/" + excelFilePath);
	//
	//		return createExcelVo;
	//    }

	//
	//	 public CreateExcelVO createExcel(HttpServletRequest request, CustomStatisticServiceInterface customStatisticService) throws Exception{
	//	    	CreateExcelVO createExcelVo = new CreateExcelVO();
	//	    	Map<String, Object> paramMap = setParam(request);
	//	    	this.customStatisticService = customStatisticService;
	//	    	List<CustomStatisticVO> list = customStatisticService.selectItems(paramMap);
	//
	//			List<GridTagVO> gridList = new ArrayList<GridTagVO>();
	//			for(int i=0; i<list.size(); i++) {
	//				GridTagVO vo = new GridTagVO();
	//				vo.setColumnId(list.get(i).getStatisticValue());
	//				vo.setColumnName(list.get(i).getStatisticName());
	//				vo.setColumnSize("100");
	//				gridList.add(vo);
	//			}
	//			paramMap.put("gridList", gridList);
	//
	//			paramMap.put("query", customStatisticService.getCountQuery(paramMap, list));
	//
	//	    	int totalCount = checkExcelCount(paramMap);
	//
	//
	//	    	String excelFilePath = getExcelFileName(rootAbsolutePath.toString());
	//
	//
	//	    	makeExcel(paramMap, excelFilePath, totalCount, list);
	//
	//	    	createExcelVo.setResult(ResultVO.RESULT_SUCCESS);
	//	    	createExcelVo.setUrl(request.getContextPath() + "/" + excelFilePath);
	//
	//	    	return createExcelVo;
	//
	//	    }
}
