package kr.esob.fdms.commonlogic.viewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;

@Repository
public class PrintAuditDao extends AbstractDao {
    private static final String PREFIX = "sql.PrintAudit.";

    public int insertJob(PrintJobVO job, UserVO actor, String clientIp) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("job", job);
        param.put("actor", actor);
        param.put("clientIp", clientIp);
        return (Integer) insert(PREFIX + "insertJob", param);
    }

    public int insertItem(PrintJobItemVO item) {
        return (Integer) insert(PREFIX + "insertItem", item);
    }

    public PrintJobVO selectJobForUpdate(String printJobId) {
        return (PrintJobVO) objNotUseSession(PREFIX + "selectJobForUpdate", printJobId);
    }

    @SuppressWarnings("unchecked")
    public List<PrintJobItemVO> selectItems(String printJobId) {
        return listNotUseSession(PREFIX + "selectItems", printJobId);
    }

    public int completeJob(PrintResultParam result, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("result", result);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "completeJob", param);
    }

    public int markCountApplied(String printJobId, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("printJobId", printJobId);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "markCountApplied", param);
    }
}
