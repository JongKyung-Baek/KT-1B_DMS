package kr.esob.tdms.commonlogic.pdfconversion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.support.ScopeNotActiveException;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.tdms.commonlogic.value.SessionValue;

class PdfConversionDaoBackgroundSafetyTest {

    @Test
    void durableJobStateUpdatesDoNotResolveTheHttpSessionScope() {
        SqlSessionTemplate sqlMap = mock(SqlSessionTemplate.class);
        SessionValue session = unavailableSession();
        PdfConversionDao dao = new PdfConversionDao();
        inject(dao, sqlMap, session);
        Map<String, Object> values = new HashMap<String, Object>();

        dao.markSucceeded(values);
        dao.markRetry(values);
        dao.markFailed(values);
        dao.markNotRequired(values);

        verify(sqlMap).update("sql.PdfConversion.markSucceeded", values);
        verify(sqlMap).update("sql.PdfConversion.markRetry", values);
        verify(sqlMap).update("sql.PdfConversion.markFailed", values);
        verify(sqlMap).update("sql.PdfConversion.markNotRequired", values);
        verify(session, never()).getSessionLang();
    }

    @Test
    void projectionUpdatesDoNotResolveTheHttpSessionScope() {
        SqlSessionTemplate sqlMap = mock(SqlSessionTemplate.class);
        SessionValue session = unavailableSession();
        PdfConversionProjectionDao dao = new PdfConversionProjectionDao();
        inject(dao, sqlMap, session);
        Map<String, Object> values = new HashMap<String, Object>();

        dao.updateStatus(values);
        dao.reconcileCurrentSw();
        dao.reconcileCurrentSwSub();

        verify(sqlMap).update("sql.PdfConversionProjection.updateStatus", values);
        verify(sqlMap).update("sql.PdfConversionProjection.reconcileCurrentSw", null);
        verify(sqlMap).update("sql.PdfConversionProjection.reconcileCurrentSwSub", null);
        verify(session, never()).getSessionLang();
    }

    private SessionValue unavailableSession() {
        SessionValue session = mock(SessionValue.class);
        when(session.getSessionLang()).thenThrow(new ScopeNotActiveException(
                "scopedTarget.session", "session",
                new IllegalStateException("No thread-bound request found")));
        return session;
    }

    private void inject(Object dao, SqlSessionTemplate sqlMap, SessionValue session) {
        ReflectionTestUtils.setField(dao, "sqlMap", sqlMap);
        ReflectionTestUtils.setField(dao, "session", session);
    }
}
