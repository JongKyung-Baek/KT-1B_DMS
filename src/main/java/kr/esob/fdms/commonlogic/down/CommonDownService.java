package kr.esob.fdms.commonlogic.down;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
		String tarPath = SystemConfig.getSystemConfigValue("UPDOWN_PATH").replaceAll("/", "\\\\");
		String srcFileName = "";
		String tarFileName = "";

		for(CommonDownFileVO tempVO : tempList) {
			
			try {
				srcFileName = orgPath + tempVO.getFilePathNm().replaceAll("/", "\\\\");
				tarFileName = tarPath + tempVO.getFileNm();

				File in = new File(srcFileName);
				File out = new File(tarFileName);
			
				FileCopyUtils.copy(in, out);
				
				tempVO.setFilePathNm(tarPath);
				rtnList.add(tempVO);
				
			}catch(Exception e) {
				e.printStackTrace();
			
			}
		}

		log.info(JSONArray.fromObject(rtnList).toString());

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
