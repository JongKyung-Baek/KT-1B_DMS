package kr.esob.fdms.commonlogic.GarbageCleaner;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GarbageCleanerService 
{
	@Inject
	GarbageCleanerDao dao;

	@Inject
	CommonRequestService commonRequestService;	

	public void GarbageCleaner() throws Exception 
	{
		try 
		{
			//config table에서, 대상파일 경로 조희
			List<Map<String,Object>> dbConfig = dao.selectDbConfig();
			String strMergePath="";
			
			for(Map<String,Object> config : dbConfig) {
				if(config.get("SYSTEM_CONFIG_CD").equals("MERGE_PATH")) {
					strMergePath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
			}		
			//System.out.println("strMergePath = " + strMergePath);	
			
			//오늘 날짜인 폴더 
			Calendar cal = Calendar.getInstance();
		    String dateString;
		    dateString = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
		    
			// mergeFile 모든 폴더 목록을 가져온다
			String isDir = strMergePath;
			String isDir2 = "";
			try 
			{
		        for (File info : new File(isDir).listFiles()) 
		        {
		            if (info.isDirectory()) {	                
		                if (info.getName().equals(dateString))
		                {	
		                	//System.out.println("하위폴더명= " + info.getName());
		                }
		                else
		                {
		                	isDir2 = isDir + "\\" +info.getName();
		                	
		                	for (File info2 : new File(isDir2).listFiles()) 
		                	{
		        	            if (info2.isFile()) {
		        	            	info2.delete();
		        	            }
		                	}
		                	info.delete();
		                }
		            }
		        }
			} catch (Exception e) {
				e.getStackTrace();
			}
										
		}
		catch(Exception e) 
		{
			log.error(e.getMessage());
			e.printStackTrace();
		}	
		
	}
}
