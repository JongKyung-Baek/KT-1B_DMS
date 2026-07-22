package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;


import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DocPdfLinkRequestDao extends AbstractDao {
	private String prefix = "sql.docPdfLinkRequest.";

	//Select DB Config
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectDbConfig() {
		return listNotUseSession(prefix + "selectConfig");
	}

	@SuppressWarnings("unchecked")
	public String selectFilePathNmDoc(Map<String, Object> param) {
		return (String) obj(prefix + "selectFilePathNmDoc", param);
	}

	@SuppressWarnings("unchecked")
	public String selectFilePathNmDrawing(Map<String, Object> param) {
		//System.out.println("====" );
		//System.out.println("param = " +param);
		//System.out.println("====" );
		//return (String) objNotUseSession(prefix + "selectFilePathNmDrawing");
		return (String) obj(prefix + "selectFilePathNmDrawing", param);
	}

	@SuppressWarnings("unchecked")
	public String selectFilePathNmVideo(Map<String, Object> param) {
		return (String) obj(prefix + "selectFilePathNmVideo", param);
	}

	@SuppressWarnings("unchecked")
	public String selectFilePathNmUnReg(Map<String, Object> param) {
		return (String) obj(prefix + "selectFilePathNmUnreg", param);
	}

	public String selectSwFile(Map<String, Object> param) {
		//System.out.println("====" );
		//System.out.println("param = " +param);
		//System.out.println("====" );
		//return (String) objNotUseSession(prefix + "selectFilePathNmDrawing");
		return (String) obj(prefix + "selectSwFile", param);
	}

	public String selectProduction(Map<String, Object> param) {
		//System.out.println("====" );
		//System.out.println("param = " +param);
		//System.out.println("====" );
		//return (String) objNotUseSession(prefix + "selectFilePathNmDrawing");
		return (String) obj(prefix + "selectProduction", param);
	}
	public String selectDxf(Map<String, Object> param) {
		//System.out.println("====" );
		//System.out.println("param = " +param);
		//System.out.println("====" );
		//return (String) objNotUseSession(prefix + "selectFilePathNmDrawing");
		return (String) obj(prefix + "selectDxf", param);
	}
	public String selectPeerReview(Map<String, Object> param) {
		return (String) obj(prefix + "selectPeerReview", param);
	}

	public List<DocPdfLinkRequestVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("unchecked")
	public String selectTotalPageNoDoc(Map<String, Object> param) {
		return (String) obj(prefix + "selectTotalPageNoDoc", param);
	}

	@SuppressWarnings("unchecked")
	public String selectTotalPageNoDrawing(Map<String, Object> param) {
		return (String) obj(prefix + "selectTotalPageNoDrawing", param);
	}

	public String selectInsertDt(Map<String, String> param) {
		return (String) obj(prefix + "selectInsertDt", param);
	}

	@SuppressWarnings("unchecked")
	public String selectGuid(String param) {
		return (String) obj(prefix + "selectGuid", param);
	}

	public String selectDrawingNoDrawing(Map<String, String> param) {
		return (String) obj(prefix + "selectDrawingNoDrawing", param);
	}

	public String selectDocumentNoDoc(Map<String, String> param) {
		return (String) obj(prefix + "selectDocumentNoDoc", param);
	}

	public String selectDrawingNoCP(Map<String, String> param) {
		return (String) obj(prefix + "selectDrawingNoCP", param);
	}

	public String selectDrawingNoDXF(Map<String, String> param) {
		return (String) obj(prefix + "selectDrawingNoDXF", param);
	}

	public String selectRevisionDrawing(Map<String, String> param) {
		return (String) obj(prefix + "selectRevisionDrawing", param);
	}

	public String selectRevisionCP(Map<String, String> param) {
		return (String) obj(prefix + "selectRevisionCP", param);
	}

	public String selectRevisionDXF(Map<String, String> param) {
		return (String) obj(prefix + "selectRevisionDXF", param);
	}

	public String selectFileNmDoc(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmDoc", param);
	}

	public String selectFileNmDrawing(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmDrawing", param);
	}

	public String selectFileNmCP(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmCP", param);
	}

	public String selectFileNmDXF(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmDXF", param);
	}

	public String selectSwNo(Map<String, String> param) {
		return (String) obj(prefix + "selectSwNo", param);
	}

	public String selectRevisionSW(Map<String, String> param) {
		return (String) obj(prefix + "selectRevisionSW", param);
	}

	public String selectFileNmSW(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmSW", param);
	}

	public String selectPeerReviewNo(Map<String, String> param) {
		return (String) obj(prefix + "selectPeerReviewNo", param);
	}

	public String selectFileNmPeerReview(Map<String, String> param) {
		return (String) obj(prefix + "selectFileNmPeerReview", param);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectPeerReviewAuthorityInfo(Map<String, String> param) {
		return (Map<String, Object>) obj(prefix + "selectPeerReviewAuthorityInfo", param);
	}

	// 1회용 키 DOCS_VIEWER_KEY에 삽입
	public String insertKey(String disposableKey, String objectId, java.util.Date now){
		Map<String, Object> params = new HashMap<>();
		params.put("disposableKey", disposableKey);
		params.put("objectId", objectId);
		params.put("now", now);

		return (String) obj(prefix +"insertKey", params);
	}

	// 뷰어 정보 DOCS_VIEWER_LOG 에 삽입
	public String insertViewerData(String objectId, String userNm, java.util.Date date){
		Map<String, Object> params = new HashMap<>();
		params.put("objectId", objectId);
		params.put("userNm", userNm);
		params.put("date", date);
		return (String) obj(prefix +"insertViewerData", params);
	}

	public String insertViewPrintHistory(String distributionType, String drawingNo, String objectId, String orgFileNm,
										 String revision, String userId, String userNm, String logType, java.util.Date insertDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("distributionType", distributionType);
		params.put("drawingNo", drawingNo);
		params.put("objectId", objectId);
		params.put("orgFileNm", orgFileNm);
		params.put("revision", revision);
		params.put("userId", userId);
		params.put("userNm", userNm);
		params.put("logType", logType);
		params.put("insertDate", insertDate);
		return (String) obj(prefix + "insertViewPrintHistory", params);
	}

	public String deleteKey(java.util.Date now){
		return (String) obj(prefix + "deleteKey", now);
	}

	public String getPostUrl() {
		return (String) obj(prefix + "selectPostUrl");
	}

	public List<Map<String, Object>> selectRevisionListDrawing(Map<String, String> param) {
		return (List<Map<String, Object>>) list(prefix + "selectRevisionListDrawing", param);
	}

	public List<Map<String, Object>> selectRevisionListCP(Map<String, String> param) {
		return (List<Map<String, Object>>) list(prefix + "selectRevisionListCP", param);
	}

	public List<Map<String, Object>> selectRevisionListDXF(Map<String, String> param) {
		return (List<Map<String, Object>>) list(prefix + "selectRevisionListDXF", param);
	}
//	public void updateList(Object param) {
//		update(prefix + "updateList", param);
//	}

}
