package kr.esob.fdms.commonlogic.updown;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@SuppressWarnings("unchecked")
@Repository
public class CommonUpdownDao extends AbstractDao {
	private String prefix = "sql.CommonUpdown.";

	public Map<String, Object> getUploadConfig() {
		return (Map<String, Object>) get(prefix + "getUpdownConfig");
	}

//	public List<CommonUpdownFileVO> selectFileInfo(CommonUpdownParam param) {
//		return list(prefix + "selectFileInfo", param);
//	}



	public List selectList(Object param) {
		return list(prefix + "selectList", param);
	}



	// DOWNLOAD_COUNT 데이터 가져오기
	@Autowired
	private SqlSession sqlSession;
	public Integer getDownloadCount(String orgFilePath) {
		return sqlSession.selectOne(prefix + "getDownloadCount", orgFilePath);
	}


	public Integer plusDownloadCount(String orgFilePath){
		return sqlSession.selectOne(prefix + "plusDownloadCount", orgFilePath);
	}


	public void addToDownHistory(String requestNo, String docSeq, Integer downCount, String objectNo, String objectNm, String userNm , java.util.Date downDate, String downloadedName){
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		params.put("downCount", downCount);
		params.put("objectNo", objectNo);
		params.put("objectNm", objectNm);
		params.put("userNm", userNm);
		params.put("downDate", downDate);
		params.put("downloadedName", downloadedName);
		sqlSession.insert(prefix + "addToDownHistory", params);
	}

	public Map<String, Object> getObjectNoObjectNm(String requestNo, String docSeq) {
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		return sqlSession.selectOne(prefix + "getObjectNoObjectNm", params);
	}


	public void saveLog(DownloadedFileActLogDto logData){
		insert(prefix + "insertLog", logData);
	}


	// KAI - 다운로드 로직 추가
	public String selectDocRestRequestSeq(String requestNo, String docSeq) {
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		return (String) obj(prefix + "selectDocRestRequestSeq", params);
	}

	public String selectDrawingRestRequestSeq(String requestNo, String docSeq) {
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		return (String) obj(prefix + "selectDrawingRestRequestSeq", params);
	}

	public String selectDocFileSeqByDocSeq(String requestNo, String docSeq) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("docSeq", docSeq); // DATA_OFFER_DOC_SEQ
		return (String) obj(prefix + "selectDocFileSeqByDocSeq", p);
	}

	public String selectDocSeqByDataNo(String requestNo, String dataNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("dataNo", dataNo); // DATA_NO
		return (String) obj(prefix + "selectDocSeqByDataNo", p);
	}

	public String selectDrawingFileSeqByDocSeq(String requestNo, String docSeq) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("docSeq", docSeq); // DELVY_CNFIRM_DOC_SEQ
		return (String) obj(prefix + "selectDrawingFileSeqByDocSeq", p);
	}

	public String selectDrawingDocSeqByDataNo(String requestNo, String dataNo) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("dataNo", dataNo); // DATA_NO
		return (String) obj(prefix + "selectDrawingDocSeqByDataNo", p);
	}

	public String selectDocPdfFileSeqByDocSeq(String requestNo, String docSeq) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("docSeq", docSeq); // DATA_OFFER_DOC_SEQ
		return (String) obj(prefix + "selectDocPdfFileSeqByDocSeq", p);
	}

	public String selectDrawingPdfFileSeqByDocSeq(String requestNo, String docSeq) {
		Map<String, Object> p = new HashMap<>();
		p.put("requestNo", requestNo);
		p.put("docSeq", docSeq); // DELVY_CNFIRM_DOC_SEQ
		return (String) obj(prefix + "selectDrawingPdfFileSeqByDocSeq", p);
	}

	//KAI-문서행위로그-다운로드
	public Map<String, Object> selectActLogBase(String requestNo, String docSeq, String orgFilePath, String userCd) {
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		params.put("orgFilePath", orgFilePath);
		params.put("userCd", userCd);
		return sqlSession.selectOne(prefix + "selectActLogBase", params);
	}

	public Map<String, Object> selectOutsideDistributionActLogBase(String requestNo, String docSeq, String objectType, String fileSeq) {
		Map<String, Object> params = new HashMap<>();
		params.put("requestNo", requestNo);
		params.put("docSeq", docSeq);
		params.put("objectType", objectType);
		params.put("fileSeq", fileSeq);
		return sqlSession.selectOne(prefix + "selectOutsideDistributionActLogBase", params);
	}

	public int updateOutsideDistributionDeliveryConfirmDoc(String objectId, String objectType) {
		Map<String, Object> params = new HashMap<>();
		params.put("objectId", objectId);
		params.put("objectType", objectType);
		return sqlSession.update(prefix + "updateOutsideDistributionDeliveryConfirmDoc", params);
	}

	public Integer countOutsideDistributionPendingDeliveryConfirmDocs(String objectId, String objectType) {
		Map<String, Object> params = new HashMap<>();
		params.put("objectId", objectId);
		params.put("objectType", objectType);
		return sqlSession.selectOne(prefix + "countOutsideDistributionPendingDeliveryConfirmDocs", params);
	}

	public int updateOutsideDistributionDeliveryConfirmHeader(String objectId, String objectType) {
		Map<String, Object> params = new HashMap<>();
		params.put("objectId", objectId);
		params.put("objectType", objectType);
		return sqlSession.update(prefix + "updateOutsideDistributionDeliveryConfirmHeader", params);
	}


}
