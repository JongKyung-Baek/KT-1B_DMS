package kr.esob.fdms.controller.inside.cr;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class CommonCrDao extends AbstractDao{
	private String prefix = "sql.CommonCr.";

	public CrFileVO selectInsideDownloadResource(CrFileDownloadParam param) {
		return (CrFileVO) obj(prefix + "selectInsideDownloadResource", param);
	}

	@SuppressWarnings("unchecked")
	public List<CrFileVO> selectInsideFileList(CrParam param){
		return list(prefix + "selectInsideFileList", param);
	}

}
