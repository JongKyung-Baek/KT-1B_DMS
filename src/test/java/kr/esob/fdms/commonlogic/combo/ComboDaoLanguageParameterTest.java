package kr.esob.fdms.commonlogic.combo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.fdms.commonlogic.value.SessionValue;

class ComboDaoLanguageParameterTest {

    private ComboDao dao;
    private SqlSessionTemplate sqlMap;
    private SessionValue session;

    @BeforeEach
    void setUp() {
        dao = new ComboDao();
        sqlMap = mock(SqlSessionTemplate.class);
        session = new SessionValue();
        session.setSessionLang("en");
        ReflectionTestUtils.setField(dao, "sqlMap", sqlMap);
        ReflectionTestUtils.setField(dao, "sessionValue", session);
        stubEmptyList();
    }

    @Test
    void stringAndNullParametersBecomeLocalizedComboInfoObjects() {
        dao.selectComboList("productionObjectType");
        ComboInfoVO stringParam =
                (ComboInfoVO) captureParameter("sql.Combo.selectComboList");
        assertEquals("productionObjectType", stringParam.getComboCd());
        assertEquals("en", stringParam.getSessionLang());

        resetAndStub();
        dao.selectComboList(null);
        ComboInfoVO nullParam =
                (ComboInfoVO) captureParameter("sql.Combo.selectComboList");
        assertNull(nullParam.getComboCd());
        assertEquals("en", nullParam.getSessionLang());
    }

    @Test
    void voParametersAlwaysUseTheTrustedSessionLanguage() {
        ComboInfoVO combo = new ComboInfoVO();
        combo.setComboCd("businessAreaCd");
        combo.setSessionLang("ko");

        dao.selectComboList(combo);

        Object captured = captureParameter("sql.Combo.selectComboList");
        assertSame(combo, captured);
        assertEquals("en", combo.getSessionLang());

        resetAndStub();
        ComboParamVO commonParam = new ComboParamVO();
        commonParam.setComboCd("requestPurpose");
        commonParam.setSessionLang("ko");
        dao.selectComboList(commonParam);
        assertSame(
                commonParam,
                captureParameter("sql.Combo.selectComboList"));
        assertEquals("en", commonParam.getSessionLang());
    }

    @Test
    void mapParametersAreCopiedAndLocalizedWithoutMutatingCallerInput() {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("comboCd", "requestPurpose");
        input.put("sessionLang", "ko");

        dao.selectComboList(input);

        Object captured = captureParameter("sql.Combo.selectComboList");
        assertTrue(captured instanceof Map);
        assertNotSame(input, captured);
        assertEquals("en", ((Map<?, ?>) captured).get("sessionLang"));
        assertEquals("ko", input.get("sessionLang"));
    }

    @Test
    void dedicatedQueriesReceiveNormalizedLanguageParameters() {
        dao.selectComboListByCd("distributionMethod");
        ComboInfoVO byCode =
                (ComboInfoVO) captureParameter("sql.Combo.selectComboListByCd");
        assertEquals("distributionMethod", byCode.getComboCd());
        assertEquals("en", byCode.getSessionLang());

        resetAndStub();
        dao.selectComboLang("en-US");
        Object languageParam =
                captureParameter("sql.Combo.selectComboLang");
        assertEquals("en", ((Map<?, ?>) languageParam).get("sessionLang"));

        resetAndStub();
        session.setSessionLang(null);
        dao.selectComboLang();
        Object defaultLanguageParam =
                captureParameter("sql.Combo.selectComboLang");
        assertEquals(
                "ko",
                ((Map<?, ?>) defaultLanguageParam).get("sessionLang"));
    }

    @Test
    void dynamicComboQueryReceivesSessionLanguageForAnyMapImplementation() {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("queryId", "sql.Combo.selectObjectClassCdSw");
        input.put("sessionLang", "ko");

        dao.selectCombo(input);

        Object captured =
                captureParameter("sql.Combo.selectObjectClassCdSw");
        assertEquals("en", ((Map<?, ?>) captured).get("sessionLang"));
        assertEquals("ko", input.get("sessionLang"));
    }

    private Object captureParameter(String queryId) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(sqlMap).selectList(
                org.mockito.ArgumentMatchers.eq(queryId),
                captor.capture());
        return captor.getValue();
    }

    private void resetAndStub() {
        reset(sqlMap);
        stubEmptyList();
    }

    private void stubEmptyList() {
        when(sqlMap.selectList(anyString(), any()))
                .thenReturn(Collections.emptyList());
    }
}
