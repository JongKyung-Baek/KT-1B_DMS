package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class DisposalAcceptanceService implements CommonService{

	@Inject
	DisposalAcceptanceDao dao;

	@Inject
	DateUtil dateUtil;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public DisposalAcceptancePopupVO selectDisposalInfo(DisposalAcceptanceParam param) {
		return dao.selectDisposalInfo(param);
	}

	public List<DisposalAcceptancePopupListVO> selectPopupList(DisposalAcceptanceParam param){
		return dao.selectPopupList(param);
	}

	public List<DestroyFileVO> selectDisposalFileList(DisposalAcceptanceParam param){
		return dao.selectDisposalFileList(param);
	}

	public void fileDownload(DestroyFileVO param, HttpServletResponse response) throws Exception {
		DestroyFileVO vo = dao.selectFileInfo(param);
		File file = new File(vo.getFilePath());
		String mimeType = "application/octet-stream";

		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + vo.getFileName() + "\""));
		response.setContentLength((int) file.length());
		try {
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();
            response.getOutputStream().close();
        } catch (FileNotFoundException ex) {
        	throw new FileNotFoundException();
        } catch (IOException ex) {
        	throw new IOException();
        }
	}

	public ResultVO saveApproval(DisposalAcceptanceParam param) {
		ResultVO resultVo = new ResultVO();
		dao.updateDestroyRequest(param);
		dao.updateDestroyRequestDetail(param);
		dao.updateRequestFile(param);
		dao.updateApprovalFile(param);
		resultVo.setSuccess(true);
		return resultVo;
	}

}
