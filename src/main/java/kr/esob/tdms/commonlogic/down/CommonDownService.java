package kr.esob.tdms.commonlogic.down;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.tdms.util.FileUtil;
import kr.esob.tdms.util.StoragePathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

@Slf4j
@Service
public class CommonDownService implements CommonService{

	@Inject
	CommonDownDao dao;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List selectList(Object obj) {
		List<CommonDownFileVO> rtnList = new ArrayList<CommonDownFileVO>();
		List<CommonDownFileVO> tempList = dao.selectList(obj);
		
		String orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") ;
		String tarPath = StoragePathUtils.toPath(
				SystemConfig.getSystemConfigValue("UPDOWN_PATH")).toString();
		String srcFileName = "";
		String tarFileName = "";

		for(CommonDownFileVO tempVO : tempList) {
			
			try {
				srcFileName = StoragePathUtils.resolve(orgPath, tempVO.getFilePathNm()).toString();
				tarFileName = StoragePathUtils.resolve(tarPath, tempVO.getFileNm()).toString();

				File in = new File(srcFileName);
				File out = new File(tarFileName);
			
				FileCopyUtils.copy(in, out);
				
				tempVO.setFilePathNm(tarPath);
				rtnList.add(tempVO);
				
			}catch(Exception e) {
				log.warn("Failed to prepare a legacy download file. cause={}",
					e.getClass().getSimpleName());
			
			}
		}

		return rtnList;
	}
	
	@Override
	public int selectListCount(Object obj) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public Map<String, Object> getUploadConfig() {
		return dao.getUploadConfig();
	}

}
