package kr.esob.fdms.commonlogic.FromAdapToItn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;


@Repository
public class FromAdapToItnDao extends AbstractDao {
	private String prefix = "sql.fromAdapToItn.";

	public enum ADAP_TYPE {
		TEMP(
				"ADAP_TEMP",
				"ADAP_REPO",
				"insertAdapTemp",
				"",
				"deleteAdapTemp"),
		REPO(
				"ADAP_TEMP",
				"ADAP_REPO",
				"insertAdapRepo",
				"updateAdapRepo",
				""),
		REPO2(
				"ADAP_TEMP",
				"ADAP_REPO",
				"",
				"updateRepo2",
				""),
		DOCUMENT(
				"ADAP_DOCUMENT",
				"ITN_DOCUMENT",
				"insertItnDocument",
				"updateItnDocument",
				""),
		DOCUMENT_FILE(
				"ADAP_DOCUMENT_FILE",
				"ITN_DOCUMENT_FILE",
				"insertItnDocumentFile2",
				"updateItnDocumentFile",
				""),
		DOCUMENT_MEMBER(
				"ADAP_DOCUMENT_MEMBER",
				"ITN_DOCUMENT_MEMBER",
				"insertItnDocumentMember",
				"",
				"deleteItnDocumentMember"),
		ADAP_DOCUMENT(
				"ADAP_REPO",
				"ADAP_DOCUMENT",
				"insertAdapDocumentB6",
				"updateAdapDocument",
				""),

		ADAP_DOCUMENT2(
				"ADAP_REPO",
				"ADAP_DOCUMENT",
				"insertAdapDocument",
				"updateAdapDocument",
				""),
		ADAP_DOCUMENT_FILE(
				"ADAP_REPO",
				"ADAP_DOCUMENT_FILE",
				"insertAdapDocumentFileB6",
				"updateAdapDocumentFile",
				""),
		ADAP_DOCUMENT_MEMBER(
				"ADAP_REPO",
				"ADAP_DOCUMENT_MEMBER",
				"insertAdapDocumentMember",
				"updateAdapDocumentMember",
				""),
		ADAP_DRAWING(
				"ADAP_REPO",
				"ADAP_DRAWING",
				"insertAdapDrawingB6",
				"updateAdapDrawing",
				""),

		ADAP_SW(
				"ADAP_REPO",
				"ADAP_SW",
				"insertAdapSW",
				"updateAdapSW",
				""),


		ADAP_DRAWING_MEMBER(
				"ADAP_REPO",
				"ADAP_DRAWING",
				"insertAdapDrawingMember",
				"updateAdapDrawingMember",
				""),
		DOCS_DOCUMENT(
				"ADAP_DOCUMENT",
				"DOCS_DOCUMENT",
				"insertDocsDocumentB6",
				"updateDocsDocumentB",
				""),
		DOCS_DOCUMENT_FILE(
				"ADAP_DOCUMENT_FILE",
				"DOCS_DOCUMENT_FILE",
				"insertDocsDocumentFileB6",
				"updateDocsDocumentFileB",
				""),
		DOCS_DOCUMENT_MEMBER(
				"ADAP_DOCUMENT_MEMBER",
				"DOCS_DOCUMENT_MEMBER",
				"insertDocsDocumentMember",
				"updateDocsDocumentMember",
				""),
		DOCS_DRAWING(
				"ADAP_DRAWING",
				"DOCS_DRAWING",
				"insertDocsDrawingB6",
				"updateDocsDrawingB",
				""),
		DOCS_DRAWING_MEMBER(
				"ADAP_DRAWING_MEMBER",
				"DOCS_DRAWING_MEMBER",
				"insertDocsDrawingMember",
				"updateDocsDrawingMember",
				""),

		DOCS_PRODUCT_DOCUMENT(
				"ADAP_SW",
				"DOCS_SW",
				"insertProduction",
				"updateProduction",
				""),

		DOCS_PRODUCT_DOCUMENT_FILE(
				"ADAP_SW",
				"DOCS_SW",
				"insertProductionFile",
				"updateProductionFile",
				""),

		DOCS_SW(
				"ADAP_SW",
				"DOCS_SW",
				"insertSW",
				"updateSW",
				""),

		DOCS_SW_FILE(
				"ADAP_SW",
				"DOCS_SW",
				"insertSWFile",
				"updateSWFile",
				""),

		DOCS_DOCUMENT_V2(
				"ADAP_SW",
				"DOCS_SW",
				"insertDocsDocumentV2",
				"updateDocsDocumentV2",
				""),


		DOCS_DOCUMENT_FILE_V2(
				"ADAP_SW",
				"DOCS_SW",
				"insertDocsDocumentFile",
				"updateDocsDocumentFile",
				""),

		DOCS_DRAWING_FILE_V2(
				"ADAP_SW",
				"DOCS_SW",
				"selectDocsDrawingV2",
				"updateDocsDrawingV2",
				""),

		DOCS_DRAWING_V2(
				"ADAP_SW",
						"DOCS_SW",
						"insertDocsDrawingV2",
						"updateDocsDrawingV2",
						"");



		ADAP_TYPE(String fromTable, String toTable, String insert, String update, String delete) {
			this.fromTableName = fromTable;
			this.toTableName = toTable;		
			this.insertQueryId = insert;
			this.updateQueryId = update;
			this.deleteQueryId = delete;
		}

		private String fromTableName;
		private String toTableName;
		private String insertQueryId;
		private String updateQueryId;
		private String deleteQueryId;

		public String getFromTableName() {
			return fromTableName;
		}
		
		public String getToTableName() {
			return toTableName;
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

	//List의 내용을 임시테이블(파일경로, 파일사이즈만 있음)에 입력
	
	public void insertTempFilePath2 (ADAP_TYPE objectType, List<Map<String, Object>> list)
	 {
	    Map<String, Object> map = new HashMap<>();
	    map.put("list", list);
	    insert(prefix + objectType.getInsertQueryId(), map);
	}
	
	public void insertTempFilePath(ADAP_TYPE objectType, Map<String, Object> param) {
		
		//param.put("TABLE_NAME", objectType.getFromTableName());
		//System.out.println("====" );
		//System.out.println("param = " +param);
		//System.out.println("====" );
		insert(prefix + objectType.getInsertQueryId(), param);
		
	}
	
	//db 임시 테이블에서 기존 REPO 테이블과 비교해서 업데이트 대상을 찾음, repoDB에서 objectType, uid 부여
	public void insertRepo(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	
	
	// Temp db 삭제
	public void deleteTemp(ADAP_TYPE objectType, String param) {
		delete(prefix + objectType.getDeleteQueryId(), param);
	}
				
	// REPO에서 ITN으로 인서트, 파일경로 uid/파일명으로 복사
	

	
	
	// Repo 데이터 입력
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectNewDataFromRepo(ADAP_TYPE type, Integer unit) {
		Map<String,Object> param = new HashMap<String, Object>();

		param.put("unit", unit);

		param.put("TABLE_NAME", type.getFromTableName());
		return listNotUseSession(prefix + "selectNewDataRepo", param);
	}
	

	//Update ADAP
	public void updateRepo(ADAP_TYPE objectType, Map<String, Object> param) {

		System.out.println("====" );
		System.out.println("param = " +param);
		System.out.println("====" );
		
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}

	public void updateRepo(ADAP_TYPE objectType, String param) {

		System.out.println("====" );
		System.out.println("param = " +param);
		System.out.println("====" );

		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}
	
	//REPO_TO_ADAP 대상 데이터 조회
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectRepoDataFromRepo(ADAP_TYPE type) {
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("TABLE_NAME", type.getFromTableName());
		return listNotUseSession(prefix + "selectRepoToADAP", param);
	}

	public List<Map<String, Object>> selectTempFile(Map<String, Object> param) {


		return listNotUseSession(prefix + "selectTempFile", param);
	}
	
	//REPO에서 ADAP로 넣을 데이터 생성
	public void insertAdapDocumnet(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	public void insertAdapDocumnetFile(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	//public void insertAdapDocumnetMember(ADAP_TYPE objectType, String param) {
	//	insert(prefix + objectType.getInsertQueryId(), param);
	//}
	public void insertAdapDrawing(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}
	//REPO_TO_ADAP 대량 업데이트	
	public void updateAdapRepo(ADAP_TYPE objectType, String param) {		
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}
	/*
	public void insertAdapDocumnet(ADAP_TYPE objectType, Map<String, Object> param) {
		param.put("FROM_TABLE_NAME", objectType.getFromTableName());
		param.put("TO_TABLE_NAME", objectType.getToTableName());
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	public void insertAdapDocumnetFile(ADAP_TYPE objectType, Map<String, Object> param) {
		param.put("FROM_TABLE_NAME", objectType.getFromTableName());
		param.put("TO_TABLE_NAME", objectType.getToTableName());
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	public void insertAdapDocumnetMember(ADAP_TYPE objectType, Map<String, Object> param) {
		param.put("FROM_TABLE_NAME", objectType.getFromTableName());
		param.put("TO_TABLE_NAME", objectType.getToTableName());
		insert(prefix + objectType.getInsertQueryId(), param);
	}*/	
	
	
	//ADAP에서 ITN대신 DOCS에 입력
	public void insertDocsDocumnet(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	public void insertDocsDocumnetFile(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}	
	//public void insertAdapDocumnetMember(ADAP_TYPE objectType, String param) {
	//	insert(prefix + objectType.getInsertQueryId(), param);
	//}
	public void insertDocsDrawing(ADAP_TYPE objectType, String param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}
	//REPO_TO_ADAP 대량 업데이트
	/*
	public void updateItnAdapDocument(ADAP_TYPE objectType, String param) {
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}
	public void updateItnAdapDocumentFile(ADAP_TYPE objectType, String param) {
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}
	public void updateItnAdapDrawing(ADAP_TYPE objectType, String param) {
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}

	 */



	//REPO_TO_ADAP 업데이트
	public void updateRepoToAdap(ADAP_TYPE objectType, Map<String, Object> param) {
		
		System.out.println("====" );
		System.out.println("param = " +param);
		System.out.println("====" );
		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}
	
	//REPO_TO_ADAP 업데이트 대량
	//public void updateRepoToAdap(ADAP_TYPE objectType, String param) {
	// int result = update(prefix + objectType.getUpdateQueryId(), param);
		// System.out.println("result : " + result);
	//}
	
	
	//Select New Data
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectNewDataFromAdap(ADAP_TYPE type, Integer unit) {
		Map<String,Object> param = new HashMap<String, Object>();

		param.put("unit", unit);

		//if(type == ADAP_TYPE.ITEM) {
		//	return listNotUseSession(prefix + "selectNewItemItn", param);
		//}

		//param.put("TABLE_NAME", type.getItnTableName());
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
	public List<Map<String, Object>> selectNewDataFromTemp(ADAP_TYPE type, Integer unit) {
		Map<String,Object> param = new HashMap<String, Object>();
		param.put("unit", unit);
		// param.put("TABLE_NAME", type.getItnTableName());
		return listNotUseSession(prefix + "selectNewDataTemp", param);
	}


	//Update Moved Data flag
	public void updateMovedData(ADAP_TYPE objectType, Map<String, Object> param, Boolean success) {
		if(success) {
			param.put("RESULT", "Y");
		}else {
			param.put("RESULT", "E");
		}
		// param.put("TABLE_NAME", objectType.getAdapTableName());

		// if(objectType == ADAP_TYPE.ITEM) {
		//	update(prefix + "updateMovedItem", param);
		//}else {
		//	update(prefix + "updateMovedData", param);
		//}

		//System.out.println("result : " + result);
	}

	//File Migration용
	public void updateFileMoveResult(ADAP_TYPE objectType, Map<String, Object> param, Boolean success) {

		if(success) {
			param.put("RESULT", "Y");
		}else {
			param.put("RESULT", "E");
		}
		//param.put("TABLE_NAME", objectType.getAdapTableName());
		update(prefix + "updateFileMoveResult", param);
	}


	//File Migration 용
	public void updateFilePath(ADAP_TYPE objectType, Map<String, Object> param) {
		//param.put("TABLE_NAME", objectType.getAdapTableName());
		update(prefix + objectType.getInsertQueryId(), param);
	}

	//Get ITN Object Count
	public Integer selectItnObjectCount(ADAP_TYPE objectType, Map<String, Object> param){


		//if(objectType == ADAP_TYPE.ITEM) {
		//	return (Integer) objNotUseSession(prefix + "selectItnItemCount" ,param);
		//}

		//param.put("TABLE_NAME", objectType.getAdapTableName());
		return (Integer) objNotUseSession(prefix + "selectItnObjectCount" ,param);
	}


	//Insert New Data To ITN
	public void insertDataToItn(ADAP_TYPE objectType, Map<String, Object> param) {
		insert(prefix + objectType.getInsertQueryId(), param);
	}

	//Update ITN
	public void updateItn(ADAP_TYPE objectType, Map<String, Object> param) {
		switch(objectType) {
		case DOCUMENT_MEMBER:
			System.out.println("Not suppoerted ObjectType");
			return;
		default:
			break;
		}

		int result = update(prefix + objectType.getUpdateQueryId(), param);
		System.out.println("result : " + result);
	}

	//Delete Itn
	public void deleteFromItn(ADAP_TYPE objectType, Map<String, Object> param) {
		switch(objectType) {
		case DOCUMENT:
		case DOCUMENT_FILE:
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
	public void insertError(ADAP_TYPE objectType, Map<String, Object> param, String error) {
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
	public void deleteError(ADAP_TYPE objectType, Map<String, Object> param) {

		//param.put("TABLE_NAME",objectType.getAdapTableName());
		delete(prefix + "deleteError", param);
	}

	public void insertItnRequest(Map<String, Object> param) {
		insert(prefix + "insertItnRequest", param);
	}

	public void insertItnRequestMapping(Map<String, Object> param) {
		insert(prefix + "insertItnRequestMapping", param);
	}

	public void insertItnRequestDetail(Map<String, Object> param) {
		insert(prefix + "insertItnRequestDetail", param);
	}

	public void insertItnRequestDeploy(Map<String, Object> param) {
		insert(prefix + "insertItnRequestDeploy", param);
	}

	public void insertItnRequestFile(Map<String, Object> param) {
		insert(prefix + "insertItnRequestFile", param);
	}

	public Map<String, String> selectDocumentFileInfo(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDocumentFileInfo", param);
	}

	public Map<String, String> selectDocsDrawing(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDocsDrawingV2", param);
	}
	public Map<String, String> selectDocsDocumentFile(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDocsDocumentV2", param);
	}
	public Map<String, String> selectSWFile(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectSWFile", param);
	}
	public Map<String, String> selectDocsProduction(Map<String, Object> param) {
		return (Map<String, String>) objNotUseSession(prefix + "selectDocsProduction", param);
	}
}