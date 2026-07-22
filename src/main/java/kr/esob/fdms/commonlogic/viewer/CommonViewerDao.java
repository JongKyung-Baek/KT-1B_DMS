package kr.esob.fdms.commonlogic.viewer;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@SuppressWarnings("unchecked")
@Repository
public class CommonViewerDao extends AbstractDao {
	private String prefix = "sql.CommonViewer.";
//
//	@SuppressWarnings("unchecked")
//	public List<CommonViewerVO> selectFileInfo(CommonViewerParam param) {
//		return list(prefix + "selectFileInfo", param);
//	}
	
	public CommonViewerVO selectFileInfo(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "selectFileInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<CommonViewerVO> selectObjectFileInfo(CommonViewerParam param) {
		return list(prefix + "selectObjectFileInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<CommonViewerVO> selectUnregFileInfo(CommonViewerParam param) {
		return list(prefix + "selectUnregFileInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<CommonViewerVO> selectProductFileInfo(CommonViewerParam param) {
		return list(prefix + "selectProductFileInfo", param);
	}

	public List<CommonViewerVO> selectList(Object param) {
		return null;
	}

	public CommonViewerVO getObjectFileInfo(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "getObjectFileInfo", param);
	}

	public CommonViewerVO getUnregFileInfo(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "getUnregFileInfo", param);
	}

	public CommonViewerVO getProductFileInfo(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "getProductFileInfo", param);
	}

	public CommonViewerVO getFileInfo(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "getFileInfo", param);
	}

	public CommonViewerVO getFileInfoForDestroyStatus(CommonViewerParam param) {
		return (CommonViewerVO) obj(prefix + "getFileInfoForDestroyStatus", param);
	}

	public String getFileInfoForDestroyStatus_printHistory(CommonViewerParam param) {
		return (String) obj(prefix + "getFileInfoForDestroyStatus_printHistory", param);
	}

	public List<WatermarkVO> selectWatermarkInfo(CommonViewerParam param) {
		return list(prefix + "selectWatermarkInfo", param);
	}

	public String getMessageDesc(CommonViewerParam param) {
		String rtn = (String) obj(prefix + "getMessageDesc", param);
		return null == rtn ? "" : rtn;
	}

	public List<CommonViewerVO> selectFileList(CommonViewerParam param) {
		return list(prefix + "selectFileList", param);
	}

	public void updatePrintCnt(CommonViewerParam param) {
		update(prefix + "updatePrintCnt", param);
	}

	public void updateMergePrintCnt(Map<String, Object> param) {
		update(prefix + "updateMergePrintCnt4", param);
	}
}
