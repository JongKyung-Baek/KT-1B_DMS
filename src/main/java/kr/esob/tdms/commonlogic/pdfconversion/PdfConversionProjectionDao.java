package kr.esob.tdms.commonlogic.pdfconversion;

import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PdfConversionProjectionDao extends AbstractDao {
    private static final String PREFIX = "sql.PdfConversionProjection.";

    public int updateStatus(Map<String, Object> values) {
        return updateNotUseSession(PREFIX + "updateStatus", values);
    }

    public int reconcileCurrentSw() {
        return updateNotUseSession(PREFIX + "reconcileCurrentSw", null);
    }

    public int reconcileCurrentSwSub() {
        return updateNotUseSession(PREFIX + "reconcileCurrentSwSub", null);
    }
}
