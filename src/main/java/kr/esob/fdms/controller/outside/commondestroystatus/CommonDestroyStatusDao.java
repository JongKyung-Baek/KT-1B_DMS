package kr.esob.fdms.controller.outside.commondestroystatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class CommonDestroyStatusDao extends AbstractDao {
	private String prefix = "sql.DocsDestroyStatus.";

	public DestroyStatusInfoVO selectDestroyStatus(DestroyStatusParam param) {
		return (DestroyStatusInfoVO) obj(prefix + "selectDestroyStatus", param);
	}

	@SuppressWarnings("unchecked")
	public List<DestroyFileVO> selectDestroyFileList(DestroyStatusInfoVO param){
		return list(prefix + "selectDestroyFileList", param);
	}

	public DestroyFileVO selectFilePath(DestroyFileVO param) {
		return (DestroyFileVO) obj(prefix + "selectFilePath", param);
	}
}
