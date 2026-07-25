package kr.esob.fdms.controller.inside.cr;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.cr.request.CrRequestParam;

@Repository
public class CommonCrDao extends AbstractDao{
	private String prefix = "sql.CommonCr.";

	public CrFileVO selectInsideDownloadResource(CrFileDownloadParam param) {
		return (CrFileVO) obj(prefix + "selectInsideDownloadResource", param);
	}

	public CrFileVO selectOutsideDownloadResource(CrFileDownloadParam param) {
		return (CrFileVO) obj(prefix + "selectOutsideDownloadResource", param);
	}

	@SuppressWarnings("unchecked")
	public List<CrFileVO> selectInsideFileList(CrParam param){
		return list(prefix + "selectInsideFileList", param);
	}

	@SuppressWarnings("unchecked")
	public List<CrFileVO> selectOutsideFileList(CrParam param){
		return list(prefix + "selectOutsideFileList", param);
	}

}
