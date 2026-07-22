package kr.esob.fdms.controller.inside.distribution.approvaldetail;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DistributionApprovalDetailDao extends AbstractDao {
	private final String prefix = "sql.distributionApprovalDetail.";

	public void insertApprovalDetail(Map<String, Object> param) {
		insert(prefix + "insertApprovalDetail", param);
	}

	public int updateApprovalDetailApproved(Map<String, Object> param) {
		return update(prefix + "updateApprovalDetailApproved", param);
	}

	public List<Map<String, Object>> selectApprovalDetails(Map<String, Object> param) {
		return list(prefix + "selectApprovalDetails", param);
	}
}
