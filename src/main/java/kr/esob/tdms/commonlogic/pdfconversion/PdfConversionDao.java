package kr.esob.tdms.commonlogic.pdfconversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PdfConversionDao extends AbstractDao {
    private static final String PREFIX = "sql.PdfConversion.";

    public PdfConversionJob enqueue(PdfConversionJob job) {
        return (PdfConversionJob) objNotUseSession(PREFIX + "enqueue", job);
    }

    public PdfConversionJob selectById(String conversionId) {
        return (PdfConversionJob) objNotUseSession(
                PREFIX + "selectById", singleton("conversionId", conversionId));
    }

    public PdfConversionJob selectCurrent(Map<String, Object> params) {
        return (PdfConversionJob) objNotUseSession(PREFIX + "selectCurrent", params);
    }

    public PdfConversionJob selectReusableByHash(Map<String, Object> params) {
        return (PdfConversionJob) objNotUseSession(
                PREFIX + "selectReusableByHash", params);
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDueIds(int limit) {
        return (List<String>) (List<?>) listNotUseSession(
                PREFIX + "selectDueIds", singleton("limit", Integer.valueOf(limit)));
    }

    public PdfConversionJob claim(Map<String, Object> params) {
        return (PdfConversionJob) objNotUseSession(PREFIX + "claim", params);
    }

    @SuppressWarnings("unchecked")
    public List<PdfConversionJob> failExpiredExhausted() {
        return (List<PdfConversionJob>) (List<?>) listNotUseSession(
                PREFIX + "failExpiredExhausted", null);
    }

    public int markSucceeded(Map<String, Object> params) {
        return updateNotUseSession(PREFIX + "markSucceeded", params);
    }

    public int markRetry(Map<String, Object> params) {
        return updateNotUseSession(PREFIX + "markRetry", params);
    }

    public int markFailed(Map<String, Object> params) {
        return updateNotUseSession(PREFIX + "markFailed", params);
    }

    public int markNotRequired(Map<String, Object> params) {
        return updateNotUseSession(PREFIX + "markNotRequired", params);
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(key, value);
        return params;
    }
}
