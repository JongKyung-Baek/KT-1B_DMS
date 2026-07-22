package kr.esob.fdms.commonlogic.abstractclass;

import org.springframework.security.core.context.SecurityContextHolder;
import kr.esob.fdms.controller.login.UserVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.value.SessionValue;

public class AbstractDao {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SqlSessionTemplate sqlMap;

    @Autowired
    SessionValue session;

    @Autowired
    Prop prop;

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return (Map<String, Object>) sqlMap.selectOne(queryId, obj);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String queryId) {
        log.info("Query Id - {}", queryId);
        return (Map<String, Object>) sqlMap.selectOne(queryId);
    }

    @SuppressWarnings("unchecked")
    private Object initParam(Object obj) {
        if (obj == null) {
            return obj;
        }
        if (obj.getClass().getSimpleName().equals("HashMap")) {
            Map<String, Object> paramMap = (Map<String, Object>) obj;
            paramMap.put("sessionLang", session.getSessionLang());
            paramMap.put("sessionUser", (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return paramMap;
        }
        try {
            PropertyUtils.setSimpleProperty(obj, "sessionLang", session.getSessionLang());
        } catch (Exception e) {
        }
        return obj;
    }

    public Object obj(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        initParam(obj);
        return sqlMap.selectOne(queryId, obj);
    }

    public Object objNotUseSession(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectOne(queryId, obj);
    }

    public Object obj(String queryId) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectOne(queryId);
    }

    @SuppressWarnings({ "rawtypes" })
    public List list(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        initParam(obj);
        return sqlMap.selectList(queryId, obj);
    }

    @SuppressWarnings("rawtypes")
    public List listNotUseSession(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectList(queryId, obj);
    }

    @SuppressWarnings("rawtypes")
    public List listNotUseSession(String queryId) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectList(queryId);
    }

    public List<ComboInfoVO> comboList(String queryId, Object obj, boolean useDefaultText) {
        log.info("Query Id - {}", queryId);
        try {
            PropertyUtils.setSimpleProperty(obj, "sessionLang", session.getSessionLang());
        } catch (Exception e) {
        }

        List<ComboInfoVO> list = sqlMap.selectList(queryId, obj);
        if (!useDefaultText) {
            return list;
        }

        List<ComboInfoVO> result = new ArrayList<ComboInfoVO>();
        ComboInfoVO vo = new ComboInfoVO();
        vo.setComboVal("");
        vo.setComboLabel(prop.msg("msg.selectItem"));
        result.add(vo);
        result.addAll(list);
        return result;
    }

    public List<ComboInfoVO> comboList(String queryId, Object obj) {
        return comboList(queryId, obj, false);
    }

    public List<String> stringList(String queryId, Object obj) {
        return sqlMap.selectList(queryId, obj);
    }

    @SuppressWarnings("rawtypes")
    public List list(String queryId) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectList(queryId);
    }

    public List<Map<String, Object>> listWithPaging(String queryId, Object obj) throws Exception {
        int pageIndex = Integer.parseInt(BeanUtils.getSimpleProperty(obj, "pageIndex"));
        int pageSize = 10;
        log.info("Query Id - {}", queryId);
        return listWithPaging(queryId, obj, pageIndex, pageSize);
    }

    public List<Map<String, Object>> listWithPaging(String queryId, Object obj, int pageIndex, int pageSize) throws Exception {
        int pagingStart = pageIndex * pageSize + 1;
        int pagingEnd = pageSize;
        PropertyUtils.setSimpleProperty(obj, "start", pagingStart);
        PropertyUtils.setSimpleProperty(obj, "end", pagingEnd);
        log.info("Query Id - {}", queryId);
        return sqlMap.selectList(queryId, obj);
    }

    public Object insert(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.insert(queryId, obj);
    }

    public int update(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.update(queryId, obj);
    }

    public int delete(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.delete(queryId, obj);
    }

    public List<ComboInfoVO> comboInfoList(String queryId, Object obj) {
        log.info("Query Id - {}", queryId);
        return sqlMap.selectList(queryId, obj);
    }
}