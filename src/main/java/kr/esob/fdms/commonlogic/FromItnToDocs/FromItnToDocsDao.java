package kr.esob.fdms.commonlogic.FromItnToDocs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.inside.cr.history.HistoryListVO;
import net.sf.json.JSONObject;


@Repository
public class FromItnToDocsDao extends AbstractDao {
	private String prefix = "sql.fromItnToDocs.";

	public enum ITN_TYPE {
		DOCUMENT(
				"ITN_DOCUMENT",
				"DOCS_DOCUMENT",
				"insertDocsDocument",
				"updateDocsDocument",
				""),
		DOCUMENT_FILE(
				"ITN_DOCUMENT_FILE",
				"DOCS_DOCUMENT_FILE",
				"insertDocsDocumentFile",
				"updateDocsDocumentFile",
				""),
		DOCUMENT_FILE2( //Migration 용
				"TEMP_DOCUMENT_FILE",
				"DOCS_DOCUMENT_FILE",
				"updateFilePath",
				"updateFileMoveResult",
				""				),
		DOCUMENT_MEMBER(
				"ITN_DOCUMENT_MEMBER",
				"DOCS_DOCUMENT_MEMBER",
				"insertDocsDocumentMember",
				"",
				"deleteDocsDocumentMember"),
		DRAWING(
				"ITN_DRAWING",
				"DOCS_DRAWING",
				"insertDocsDrawing",
				"updateDocsDrawing",
				""				),
		DRAWING2( //Migration 용
				"TEMP_DRAWING_FILE",
				"DOCS_DRAWING",
				"updateFilePath",
				"updateFileMoveResult",
				""				),
		DRAWING_MEMBER(
				"ITN_DRAWING_MEMBER",
				"DOCS_DRAWING_MEMBER",
				"insertDocsDrawingMember",
				"",
				"deleteDocsDrawingMember"),
		PRODUCT_DOCUMENT(
				"ITN_PRODUCT_DOCUMENT",
				"DOCS_PRODUCT_DOCUMENT",
				"insertDocsProductDocument",
				"updateDocsProductDocument",
				""				),
		PRODUCT_DOCUMENT_FILE(
				"ITN_PRODUCT_DOCUMENT_FILE",
				"DOCS_PRODUCT_DOCUMENT_FILE",
				"insertDocsProductDocumentFile",
				"updateDocsProductDocumentFile",
				""				),
		PRODUCT_DOCUMENT_FILE2( //Migration 용
				"TEMP_PRODUCT_DOCUMENT_FILE",
				"DOCS_PRODUCT_DOCUMENT_FILE",
				"updateFilePath",
				"updateFileMoveResult",
				""				),
		PRODUCT_SW(
				"ITN_PRODUCT_SW",
				"DOCS_PRODUCT_SW",
				"insertDocsProductSw",
				"updateDocsProductSw",
				""				),
		PRODUCT_SW_FILE(
				"ITN_PRODUCT_SW_FILE",
				"DOCS_PRODUCT_SW_FILE",
				"insertDocsProductSwFile",
				"updateDocsProductSwFile",
				""),
		PRODUCT_SW_FILE2( //Migration 용
				"TEMP_PRODUCT_SW_FILE",
				"DOCS_PRODUCT_SW_FILE",
				"updateFilePath",
				"updateFileMoveResult",
				""				),
		SW(
				"ITN_SW",
				"DOCS_SW",
				"insertDocsSw",
				"updateDocsSw",
				""),
		SW_FILE(
				"ITN_SW_FILE",
				"DOCS_SW_FILE",
				"insertDocsSwFile",
				"updateDocsSwFile",
				""),
		SW_FILE2( //Migration 용
				"TEMP_SW_FILE",
				"DOCS_SW_FILE",
				"updateFilePath",
				"updateFileMoveResult",
				""				),
		SW_MEMBER(
				"ITN_SW_MEMBER",
				"DOCS_SW_MEMBER",
				"insertDocsSwMember",
				"",
				"deleteDocsSwMember"),
		ECO_HISTORY(
				"ITN_ECO_HISTORY",
				"DOCS_ECO_HISTORY",
				"insertDocsEcoHistory",
				"updateEcoHistory",
				"deleteDocsSwMember"),
		ITEM(
				"ITN_ITEM",
				"DOCS_ITEM",
				"insertDocsItem",
				"updateDocsItem",
				"");


		ITN_TYPE(String itn, String docs, String insert, String update, String delete) {
			this.itnTableName = itn;
			this.docsTableName = docs;
			this.insertQueryId = insert;
			this.updateQueryId = update;
			this.deleteQueryId = delete;
		}

		private String itnTableName;
		private String docsTableName;
		private String insertQueryId;
		private String updateQueryId;
		private String deleteQueryId;

		public String getItnTableName() {
			return itnTableName;
		}

		public String getDocsTableName() {
			return docsTableName;
		}

		public String getInsertQueryId() {
			return insertQueryId;
		}

		public String getUpdateQueryId() {
			return updateQueryId;
		}

		public String getDeleteQueryId() {
			return deleteQueryId;
		}
	}

	//Select DB Config
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectDbConfig() {
		return listNotUseSession(prefix + "selectConfig");
	}


	//Select New Data
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectNewDataFromItn(ITN_TYPE type, Integer unit) {
		Map<String,Object> param = new HashMap<String, Object>();

		param.put("unit", unit);

		if(type == ITN_TYPE.ITEM) {
			return listNotUseSession(prefix + "selectNewItemItn", param);
		}

		param.put("TABLE_NAME", type.getItnTableName());
		return listNotUseSession(prefix + "selectNewDataItn", param);
	}


	//삭제할것
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectSWFilePath() {
		Map<String,Object> param = new HashMap<String, Object>();
		return listNotUseSession(prefix + "selectSWFilePath", param);
	}

	//도면파일 삭제
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectInterfaceFileList() {
		Map<String,Object> param = new HashMap<String, Object>();
		return listNotUseSession(prefix + "selectInterfaceFileList", param);
	}


	//File Migration용 .
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectNewDataFromTemp(ITN_TYPE type, Integer unit) {
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("unit", unit);
		param.put("TABLE_NAME", type.getItnTableName());
		return listNotUseSession(prefix + "selectNewDataTemp", param);
	}


	//Update Moved Data flag
	public void updateMovedData(ITN_TYPE objectType, Map<String, Object> param, Boolean success) {
		if(success) {
			param.put("RESULT", "Y");
		}else {
			param.put("RESULT", "E");
		}
		param.put("TABLE_NAME", objectType.getItnTableName());

		if(objectType == ITN_TYPE.ITEM) {
			update(prefix + "updateMovedItem", param);
		}else {
			update(prefix + "updateMovedData", param);
		}

		//System.out.println("result : " + result);
	}

	//File Migration용
	public void updateFileMoveResult(ITN_TYPE objectType, Map<String, Object> param, Boolean success) {

		if(success) {
			param.put("RESULT", "Y");
		}else {
			param.put("RESULT", "E");
		}
		param.put("TABLE_NAME", objectType.getItnTableName());
		update(prefix + "updateFileMoveResult", param);
	}


	//File Migration 용
	public void updateFilePath(ITN_TYPE objectType, Map<String, Object> param) {
		param.put("TABLE_NAME", objectType.getDocsTableName());
		update(prefix + objectType.getInsertQueryId(), param);
	}

	//보호 대상 파일인지 조회
	@SuppressWarnings("unchecked")
	public Object selectFileProtect(ITN_TYPE type, Map<String,Object> param) {

		switch(type) {
		case DOCUMENT_FILE:
		case SW_FILE:
			//ITN_XXX_FILE Table이 아닌 ITN_XXX Table로 부터 PROTECT_YN 값 조회해야 함.
			param.put("TABLE_NAME", type.getItnTableName().replace("_FILE", ""));
			break;
		case PRODUCT_SW_FILE: //Meta Table에 관련 Flag 없음
		case PRODUCT_DOCUMENT_FILE: //Meta Table에 관련 Flag 없음
		case DOCUMENT:
		case DOCUMENT_MEMBER:
		case DRAWING:
		case DRAWING_MEMBER:
		case PRODUCT_DOCUMENT:
		case PRODUCT_SW:
		case SW:
		case SW_MEMBER:
		case ECO_HISTORY:
		default:
			System.out.println("Not suppoerted ObjectType");
			return "";
		}
		HashMap<String, Object> result = (HashMap<String, Object>) objNotUseSession(prefix + "selectFileProtect", param);
		if( result != null) {
			return result.get("PROTECT_YN");
		}
		return "";
	}

	//Get Docs Object Count
	public Integer selectDocsObjectCount(ITN_TYPE objectType, Map<String, Object> param){

		//Eco history는 CN_SERIAL이 유일한 Key이므로, 중복이 안됨.항상 Count는 0이어야 함.
		if(objectType == ITN_TYPE.ECO_HISTORY) {
			return (Integer) objNotUseSession(prefix + "selectDocsEcoCount" ,param);
		}

		if(objectType == ITN_TYPE.ITEM) {
			return (Integer) objNotUseSession(prefix + "selectDocsItemCount" ,param);
		}

		param.put("TABLE_NAME", objectType.getDocsTableName());
		return (Integer) objNotUseSession(prefix + "selectDocsObjectCount" ,param);
	}


	//Insert New Data To Docs
	public void insertDataToDocs(ITN_TYPE objectType, Map<String, Object> param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}

	//Update Docs
	public void updateDocs(ITN_TYPE objectType, Map<String, Object> param) {
		switch(objectType) {
		case DOCUMENT_MEMBER:
		case DRAWING_MEMBER:
		case SW_MEMBER:
			System.out.println("Not suppoerted ObjectType");
			return;
		default:
			break;
		}

		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}

	//Delete Docs
	public void deleteFromDocs(ITN_TYPE objectType, Map<String, Object> param) {
		switch(objectType) {
		case DOCUMENT:
		case DOCUMENT_FILE:
		case DRAWING:
		case PRODUCT_DOCUMENT:
		case PRODUCT_DOCUMENT_FILE:
		case PRODUCT_SW:
		case PRODUCT_SW_FILE:
		case SW:
		case SW_FILE:
		case ECO_HISTORY:
			System.out.println("Not suppoerted ObjectType");
			return;
		default:
			break;
		}

		delete(prefix + objectType.getDeleteQueryId(), param);
	}


	//이전중 발생한 Error 숫자 조회
	public Integer selectErrorCount(){
		return (Integer) objNotUseSession(prefix + "selectErrorTableCount",null);
	}

	//이전중 발생한 특정 에러 조회
	public Integer selectErrorCount(Map<String, Object> param){
		return (Integer) objNotUseSession(prefix + "selectErrorCount",param);
	}

	/*에러 발생시 Insert
	public void insertError(ITN_TYPE objectType, Map<String, Object> param, String error) {
		param.put("ERROR", error);
		param.put("TABLE_NAME", objectType.getItnTableName());
		if(selectErrorCount(param) > 0) {
			update(prefix + "updateError", param);
		}else {
			insert(prefix + "insertError", param);
		}
	}
	*/

	//에러 삭제
	public void deleteError(ITN_TYPE objectType, Map<String, Object> param) {

		param.put("TABLE_NAME",objectType.getItnTableName());
		delete(prefix + "deleteError", param);
	}

	/**
	 * VENDOR_CD(BIZ_NO)로 COMPANY_CD 조회
	 * @param param
	 * @return
	 */
	public String selectCompanyCd(Map<String, Object> param) {
		return (String) objNotUseSession(prefix + "selectCompanyCd", param);
	}

	/**
	 * 사업자 코드로 긴급도 배포 사용자를 구함
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> selectDeployUserInfo(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDeployUserInfo", param);
	}

	public void insertDocsRequest(Map<String, Object> param) {
		insert(prefix + "insertDocsRequest", param);
	}

	public void insertDocsRequestMapping(Map<String, Object> param) {
		insert(prefix + "insertDocsRequestMapping", param);
	}

	public void insertDocsRequestDetail(Map<String, Object> param) {
		insert(prefix + "insertDocsRequestDetail", param);
	}

	public void insertDocsRequestDeploy(Map<String, Object> param) {
		insert(prefix + "insertDocsRequestDeploy", param);
	}

	public void insertDocsRequestFile(Map<String, Object> param) {
		insert(prefix + "insertDocsRequestFile", param);
	}

	public String selectPurchaserUserCd(Map<String, Object> param) {
		return (String) objNotUseSession(prefix + "selectPurchaserUserCd", param);
	}


	public Map<String, String> selectDocumentFileInfo(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDocumentFileInfo", param);
	}
}

