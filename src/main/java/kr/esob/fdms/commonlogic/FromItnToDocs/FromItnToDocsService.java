package kr.esob.fdms.commonlogic.FromItnToDocs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.FromItnToDocs.FromItnToDocsDao.ITN_TYPE;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

@Slf4j
@Service
public class FromItnToDocsService implements CommonService{

	@Autowired private PlatformTransactionManager transactionManager;

	@Inject
	FromItnToDocsDao dao;

	@Inject
	CommonRequestService commonRequestService;

	//private Integer errorCount = 0;

	List<Map<String, Object>> quickList = new ArrayList<Map<String,Object>>();

//	private String sourceDrive = "";
//	private String protectedFilePath = "/";
//	private String generalFilePath = "/";

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return null;//dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return 0; //dao.selectListCount(param);
	}


	public void transferItnToDocs() throws Exception {
		try {
			//이전중 data이전중 발생한 error 갯수 조희
			//errorCount = dao.selectErrorCount();

			//Data를 이전할 Data Type List
			List<ITN_TYPE> targetObjects =  Arrays.asList(
					ITN_TYPE.DOCUMENT,
					ITN_TYPE.DOCUMENT_FILE,
					ITN_TYPE.DOCUMENT_MEMBER,
					ITN_TYPE.DRAWING,
					ITN_TYPE.DRAWING_MEMBER,
					ITN_TYPE.PRODUCT_DOCUMENT,
					ITN_TYPE.PRODUCT_DOCUMENT_FILE,
					ITN_TYPE.PRODUCT_SW,
					ITN_TYPE.PRODUCT_SW_FILE,
					ITN_TYPE.SW,
					ITN_TYPE.SW_FILE,
					ITN_TYPE.SW_MEMBER,
					ITN_TYPE.ECO_HISTORY,
					ITN_TYPE.ITEM

					/* 최초 Migration용, 주석 풀지 말것 By Esob 최창운
					 * Procedure를 통해 ITN_XX_FILE TABle의 DB Data 먼저 이동후
					 * 본 서비스 프로그램을 통해 File만 복사하도록 하기 위한 Type임.
					,ITN_TYPE.PRODUCT_SW_FILE2
					,ITN_TYPE.SW_FILE2
					,ITN_TYPE.DOCUMENT_FILE2
					,ITN_TYPE.DRAWING2
					,ITN_TYPE.PRODUCT_DOCUMENT_FILE2
					*/
				);

			for(ITN_TYPE objectType : targetObjects) {
				switch(objectType) {
				case DRAWING2:
				case PRODUCT_DOCUMENT_FILE2:
				case DOCUMENT_FILE2:
				case PRODUCT_SW_FILE2:
				case SW_FILE2:
					// 최초 Migration용. 그 외 사용 하면 안됨.
					transferFiles(objectType);
					break;
				default:
					transferFromItnToDocs(objectType);
				}
			}

			/* sW_FILE 재 Migration을 위한 파일 삭제 코드
			 * 파일이 각 폴더에 흩어져 있으므로 DB에서 경로 읽어와 삭제해야 함.
			int k = 0;
			String deleteFile;
			List<Map<String, Object>> pathList = dao.selectSWFilePath();
			for(Map<String, Object> path : pathList) {
				k++;
				System.out.println("############# "+ k + " ##############");

				deleteFile = this.sourceDrive+path.get("FILE_PATH_NM");
				System.out.println("Delete File :: " + deleteFile);
				File delFile = new File(deleteFile);
				boolean isDel = delFile.delete();
				System.out.println("############# RESULT : "+ isDel + " ##############");
			}
			return;
			*/
		}
		catch(Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean copyFile(String srcPath, String destPath) {
		boolean result = false;
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

		if(!srcFile.exists()) {
			return false;
		}

		if(srcFile.isFile()) {
			int count = 0;
			long totalSize = 0;
			byte[] b = new byte[1024*1024];

			FileInputStream in = null;
			FileOutputStream out = null;

			BufferedInputStream bin = null;
			BufferedOutputStream bout = null;

			try {
				in = new FileInputStream(srcFile);
				bin = new BufferedInputStream(in);

				out = new FileOutputStream(destFile);
				bout = new BufferedOutputStream(out);
				while((count = bin.read(b)) != -1  ) {
					bout.write(b,0,count);
					totalSize += count;
				}
 				result = true;
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if(bout != null) {
						bout.close();
					}
					if(out != null) {
						out.close();
					}
					if(bin != null) {
						bin.close();
					}
					if(in != null) {
						in.close();
					}
				} catch(IOException r) {
					log.error("Error in Closing");
				}
			}
			return result;
		}else {
			return false;
		}
	}

	/*
	 * 최초 Migration에서만 사용되는 함수
	 * Desc:
	 * 프로시져를 통해, ITN_XXX_FILE Table 의 Data를 모든 옴긴 이후, 프로시져 실행중 생성된 TEMP_XXX_FILE Table의 data에 따라, 파일 복사
	 */
	@Transactional
	public void transferFiles(ITN_TYPE objectType ) {


		final Integer UNIT = 1000; // UNIT 갯수만큼 처리하고 Commit

		while(true) {
			TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);

	        //ITN에 새로운 Data가 있는지 조희
	        List<Map<String, Object>> itnNewList = dao.selectNewDataFromTemp(objectType, UNIT);
			if(itnNewList.size() == 0) {
				//새로운 Data가 없으면, break 하고 return
				transactionManager.commit(transactionStatus);
				break;
			}

			try {
				Integer temp = 0;
				for(Map<String, Object> itnNew : itnNewList) {
					//Docs 에 해당 Object ID가 이미 존재 하는지 조회
					temp++;
					log.debug("######################### " + temp.toString() + " #################### ");

					boolean success = true;
					boolean needToChangeFilePath = false;

					String surceFileFullPath = itnNew.get("ORG_FULL_PATH").toString();
					String orgFileName = itnNew.get("ORG_FILE_NM").toString();
					String destDir = itnNew.get("DEST_DIR").toString();
					String drive = itnNew.get("DRIVE").toString();
					String destFileFullPath = drive + destDir + orgFileName;

					try {
						//파일 이동
						File file = new File(surceFileFullPath); //원본파일

						//파일을 저장할 경로추출 후, 해당 경로가 없으면 경로 생성
						File dir = new File(drive+destDir);
						File tempDestFile = null;

						boolean mkdir = true;
						if(!dir.exists()) {
							mkdir = dir.mkdirs();
						}else {
							//이미 폴더에 같은 이름의 파일이 있을경우 {index} 를 파일명에 추가 시킴.
							tempDestFile = new File(destFileFullPath);
							if(tempDestFile.exists()) {

								Integer fileIndex = 0;
								needToChangeFilePath = true;

								//파일명과 확장자 분리.
								Integer lastPoint = orgFileName.lastIndexOf(".");
								String FileName = (lastPoint > -1) ? orgFileName.substring(0,lastPoint) : orgFileName;
								String extName = (lastPoint > -1) ? orgFileName.substring(lastPoint,orgFileName.length()) : "";
								do {
									fileIndex++;
									String changedFileName = FileName + '{' + fileIndex + '}' + extName;
									destFileFullPath = drive + destDir + changedFileName;

									tempDestFile = new File(destFileFullPath);
								}while(tempDestFile.exists());
							}
						}

						//목적지로 파일 이동, 파일 이동후에 DB에 추가
						//if(file.renameTo(new File(destFileFullPath))) {
						//이동에서 복사로 임시 변경.
						log.debug("copy 1");
						if(mkdir && copyFile(surceFileFullPath, destFileFullPath)) {
							log.debug("copy 2");
							//File 복사 성공
							if(needToChangeFilePath) {
  								itnNew.put("NEW_PATH_NM", destFileFullPath.replace(drive, "")); //파일이 복사된 새로운 경로로 DOCS Table에 입력이 되어야 함.
								dao.updateFilePath(objectType, itnNew);
							}
							success = true;

						}else {
							//File 이동 실패
							//dao.insertError(objectType,itnNew , "FAIL_TO_MOVE_COPY");
							success = false;
						}
					}catch(Exception e) {
						//dao.insertError(objectType,itnNew , e.getMessage());
						e.printStackTrace();
						success = false;
					}
   					dao.updateFileMoveResult(objectType, itnNew, success);
				}
				transactionManager.commit(transactionStatus);
			}catch(Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
				transactionManager.rollback(transactionStatus);
			}
			break;
		}

	}

	@Transactional
	public void transferFromItnToDocs(ITN_TYPE objectType) {
		//config table에서, 파일 경로 조희
		List<Map<String,Object>> dbConfig = dao.selectDbConfig();
		String protectedFilePath="";
		String generalFilePath="/";
		String sourceDrive="/";
		for(Map<String,Object> config : dbConfig) {
			if(config.get("SYSTEM_CONFIG_CD").equals("PROTECTED_FILE_PATH")) {
				//보호 대상 저장 파일 경로
				protectedFilePath = config.get("SYSTEM_CONFIG_VALUE").toString();
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("GENERAL_FILE_PATH")) {
				//일반 파일  저장 경로
				generalFilePath = config.get("SYSTEM_CONFIG_VALUE").toString();
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("ITN_SOURCE_FILE_PATH")) {
				//Network 드라이브 이름.
				sourceDrive = config.get("SYSTEM_CONFIG_VALUE").toString();
			}
		}

		final Integer UNIT = 1000; // UNIT 갯수만큼 처리하고 Commit

		Map<String, List<Map<String, Object>>> distributeMap = new HashMap<>();

		while(true) {
			TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);

	        //ITN에 새로운 Data가 있는지 조희
	        List<Map<String, Object>> itnNewList = dao.selectNewDataFromItn(objectType, UNIT);

	        log.debug("TABLE NAME : " + objectType.getItnTableName() + "   NEW DATA : " + itnNewList.size());
	        
			if(itnNewList.size() == 0) {
				//새로운 Data가 없으면, break 하고 return
				transactionManager.commit(transactionStatus);
				break;
			}

			try {
				Integer temp = 0;
				//itn에 들어온 리스트 중 하나씩 꺼내 정보처리
				for(Map<String, Object> itnNew : itnNewList) {

					temp++; //log용
					log.debug("######################### " + temp.toString() + " ####################");
					//Docs 에 해당 Object ID가 이미 존재 하는지 조회
					// 각 Table에 설정된 Primary key로 조희.
					Boolean exist = (dao.selectDocsObjectCount(objectType,itnNew) > 0);
					//신규 Data 처리 결과 , default = true;
					boolean success = true;

					switch(objectType) {
					//파일 이동이 필요한 Table
					case DRAWING:
					case DOCUMENT_FILE:
					case PRODUCT_DOCUMENT_FILE:
					case PRODUCT_SW_FILE:
					case SW_FILE:
						{	
							if(itnNew.get("FILE_PATH_NM") != null && !itnNew.get("FILE_PATH_NM").toString().equals(" ")) {
								String filePathNm = itnNew.get("FILE_PATH_NM").toString();
								String orgFileName = itnNew.get("ORG_FILE_NM").toString();

								
								//방산기술보호 대상 FLAG 값
								String isProtect;
								if(objectType == ITN_TYPE.DRAWING) {
									//Drawing의 경우 같은 Table에 관련 Flag값이 존재.
									isProtect = (itnNew.get("PROTECT_YN") != null) ? itnNew.get("PROTECT_YN").toString() : "N";
								}else {
									// itnNewData Table에서 Flag 값 조회
									isProtect = dao.selectFileProtect(objectType, itnNew).toString();
								}
							
								//방산기술보호 대상 여부에 따라 별도 Directory에 파일 저장.
								String destFile = (isProtect.equals("Y")) ?
										filePathNm.replace("/FDMS/OUT","/FDMS/FILE"+protectedFilePath)  : // 보호대상
											filePathNm.replace("/FDMS/OUT","/FDMS/FILE"+generalFilePath); // 일반
										
								//대상 파일을 이동할때, 원본 파일 이름으로 대체.
								destFile = destFile.replace(itnNew.get("FILE_NM").toString(),orgFileName); // 보호대상

								String surceFileFullPath = sourceDrive + filePathNm; //원본 파일 Full Path
								String destFileFullPath = sourceDrive + destFile;  //이동할 장소 Full Path
								
								try {
									//파일을 저장할 경로추출 후, 해당 경로가 없으면 경로 생성
									Integer lastIndex = destFileFullPath.lastIndexOf("/");
									String destDir = (lastIndex > -1) ? destFileFullPath.substring(0,lastIndex+1) : "";
									File dir = new File(destDir);
									File tempDestFile = null;

									boolean mkdir = true;
									if(!dir.exists()) {
										//Directory가 없으면 Dir 생성
										if((mkdir = dir.mkdirs()) == false) {
											//Dir 생성 실패
											log.error("FAIL TO MAKE DIR : " + destDir);
										};
									}else {
										//이미 폴더에 같은 이름의 파일이 있을경우 {index} 를 파일명에 추가 시킴.
										tempDestFile = new File(destFileFullPath);
										if(tempDestFile.exists()) {
											Integer fileIndex = 0;

											//파일명과 확장자 분리.
											Integer lastPoint = orgFileName.lastIndexOf(".");
											String FileName = (lastPoint > -1) ? orgFileName.substring(0,lastPoint) : orgFileName;
											String extName = (lastPoint > -1) ? orgFileName.substring(lastPoint,orgFileName.length()) : "";
											do {
												fileIndex++;
												String changedFileName = FileName + '{' + fileIndex + '}' + extName;
												destFileFullPath = destDir + changedFileName;

												tempDestFile = new File(destFileFullPath);
											}while(tempDestFile.exists());
										}
									}

									//목적지로 파일 이동, 파일 이동후에 DB에 추가
									/* File 이동에서 복사로 임시 변경.
									File file = new File(surceFileFullPath); //원본파일
									if(file.renameTo(new File(destFileFullPath))) {
									*/
									if(mkdir && copyFile(surceFileFullPath, destFileFullPath)) {
										//File 이동 성공
										itnNew.put("FILE_PATH_NM", destFileFullPath.replace(sourceDrive, "")); //파일이 복사된 새로운 경로로 DOCS Table에 입력이 되어야 함.

										if(exist) {
											//Data가 존재하므로 Update
											dao.updateDocs(objectType, itnNew);
										}else {
											//Data가 없으므로 Insert.
											dao.insertDataToDocs(objectType,itnNew);
										}
									}else {
										//File 이동 실패
										//dao.insertError(objectType,itnNew , "FAIL_TO_MOVE_FILE");
										log.error("FAIL TO COPY : " + surceFileFullPath);
										success = false;
									}
								}catch(Exception e) {
									//dao.insertError(objectType,itnNew , e.getMessage());
									e.printStackTrace();
									success = false;
								}
							} else {
								/*파일 경로 정보가 없을 경우 그냥 Data만 이전.
								if(exist) {
									//Data가 존재하므로 Update
									dao.updateDocs(objectType, itnNew);
								}else {
									//Data가 없으므로 Insert.
									dao.insertDataToDocs(objectType,itnNew);
								}*/

								/* OR Error 처리 */
								log.error("FILE PATH IS NULL");
								success = false;
							}
						}
						break;
					case DOCUMENT_MEMBER:
					case DRAWING_MEMBER:
					case SW_MEMBER:
						{
							if(itnNew.get("FLAG") == null) {
								//dao.insertError(objectType, itnNew, "FLAG_IS_NULL");
								//Flag가 Null이므로 추가할지 삭제할지 알수 없어, 처리 불가.
								success = false;
								break;
							}
							String flag = itnNew.get("FLAG").toString();

							if(flag.equals("A")) {
								//추가
								if(exist == false) {
									dao.insertDataToDocs(objectType,itnNew);
								}else {
									//dao.insertError(objectType, itnNew, "ALREADY_EXIST_UID");
									//이미 존재하는 사용자를 다시 추가 시도.
									success = false;
								}
							}else if(flag.equals("D")) {
								//삭제
								if(exist == true) {
									dao.deleteFromDocs(objectType,itnNew);
								}else {
									//dao.insertError(objectType, itnNew, "ALREADY_DELETED_UID");
									//없는 사용자를 제거 시도
									success = false;
								}
							}else {
								//A, D 외에 Flag값 처리 불가.
								//dao.insertError(objectType, itnNew, "NOT_SUPPORTED_FLAG");
								success = false;
							}
						}
						break;
					default:
						{
							//File Table과 Member Table을 제외한 일반 Table들 처리.
							if(exist) {
								//Data가 존재하므로 Update
								dao.updateDocs(objectType, itnNew);
							}else {
								//Data가 없으므로 Insert.
								dao.insertDataToDocs(objectType,itnNew);
							}
						}
						break;
					}

					// 긴급도 자동 배포요청 처리  :: 긴급도 여부 Y => 배포처리
					if(itnNew.get("DISTRIBUTION_YN") != null && itnNew.get("DISTRIBUTION_YN").equals("Y")) {
//					if(itnNew.get("DISTRIBUTE_TYPE_CD").equals("hdQuickChangeAction") && itnNew.get("DISTRIBUTION_YN") != null && itnNew.get("DISTRIBUTION_YN").equals("Y")) {
						switch(objectType) {
						case DRAWING:
						case DOCUMENT:
						case SW:
							//방산보호 대상 여부
							String isProtect = (itnNew.get("PROTECT_YN") != null) ? itnNew.get("PROTECT_YN").toString() : "N";
							Map<String, String> userInfo = dao.selectDeployUserInfo(itnNew);

							itnNew.put("APPROVAL_LINE_ID", isProtect.equals("Y") ? 1 : 2);	// 결재라인 :: 방산=1, else=2
							itnNew.put("DEPLOY_COMPANY_CD", dao.selectCompanyCd(itnNew));	// 배포업체
							itnNew.put("DEPLOY_USER_CD", userInfo.get("USER_CD"));			// 요청자(긴급도 배포 담당자)
							itnNew.put("DEPLOY_USER_EMAIL", userInfo.get("EMAIL"));			// 요청자 EMAIL(긴급도 배포 담당자)
							itnNew.put("ACCEPTANCE_USER_CD", dao.selectPurchaserUserCd(itnNew));	//승인자
							itnNew.put("USE_START_YMD", DateUtil.getToday("yyyyMMdd"));		//사용시작일 :: today
							itnNew.put("USE_END_YMD", DateUtil.getAddMonth(6, "yyyyMMdd"));	//사용 종료일 :: today+6
							break;
							default:
								break;
						}

						String key ;
						String fileNo;
						String filePathName;
						String orgFileName;
						String fileName;
						String fileSize;
						switch(objectType) {
						case DRAWING:	//도면
							itnNew.put("OBJECT_TYPE", "DRAWING");
							itnNew.put("objectTypeCode", "D");
							itnNew.put("OBJECT_NO", itnNew.get("DRAWING_NO"));
							itnNew.put("OBJECT_NM", itnNew.get("DRAWING_NM"));
							key = itnNew.get("ECN_NO").toString() + itnNew.get("VENDOR_CD");

							if(null == distributeMap.get(key)) {
								List<Map<String, Object>> list = new ArrayList<>();
								list.add(itnNew);
								distributeMap.put(key, list);
							}
							else {
								distributeMap.get(key).add(itnNew);
							}
							
							break;
						case DOCUMENT:	//문서
							itnNew.put("OBJECT_TYPE", "DOC");
							itnNew.put("objectTypeCode", "G");
							itnNew.put("OBJECT_NO", itnNew.get("DOCUMENT_NO"));
							itnNew.put("OBJECT_NM", itnNew.get("DOCUMENT_NM"));
							key = itnNew.get("ECN_NO").toString() + itnNew.get("VENDOR_CD");
							if(null == distributeMap.get(key)) {
								List<Map<String, Object>> list = new ArrayList<>();
								list.add(itnNew);
								distributeMap.put(key, list);
							}
							else {
								distributeMap.get(key).add(itnNew);
							}
							quickList.add(itnNew);
							break;
						case SW:		//소프트웨어
							itnNew.put("OBJECT_TYPE", "SW");
							itnNew.put("objectTypeCode", "S");
							itnNew.put("OBJECT_NO", itnNew.get("SW_NO"));
							itnNew.put("OBJECT_NM", itnNew.get("SW_NM"));
							key = itnNew.get("ECN_NO").toString() + itnNew.get("VENDOR_CD");
							if(null == distributeMap.get(key)) {
								List<Map<String, Object>> list = new ArrayList<>();
								list.add(itnNew);
								distributeMap.put(key, list);
							}
							else {
								distributeMap.get(key).add(itnNew);
							}
							quickList.add(itnNew);
							break;
						case DOCUMENT_FILE:	//문서파일
							//세팅된 파일 옵션들
							fileNo = itnNew.get("FILE_NO").toString();
							filePathName = itnNew.get("FILE_PATH_NM").toString();
							orgFileName = itnNew.get("ORG_FILE_NM").toString();
							fileName = itnNew.get("FILE_NM").toString();
							fileSize = itnNew.get("FILE_SIZE").toString();
							//DOCUMENT에 묶여서 동작
							itnNew = getItnNew(itnNew, quickList);
							itnNew.put("FILE_NO", fileNo);
							itnNew.put("FILE_PATH_NM", filePathName);
							itnNew.put("ORG_FILE_NM", orgFileName);
							itnNew.put("FILE_NM", fileName);
							itnNew.put("FILE_SIZE", fileSize);
							itnNew.put("USE_START_YMD", DateUtil.getToday("yyyyMMdd"));
							itnNew.put("USE_END_YMD", DateUtil.getAddMonth(6, "yyyyMMdd"));
							
							dao.insertDocsRequestFile(itnNew);
							break;
						case SW_FILE:	//소프트웨어파일
//							//세팅된 파일 옵션들
							fileNo = itnNew.get("FILE_NO").toString();
							filePathName = itnNew.get("FILE_PATH_NM").toString();
							orgFileName = itnNew.get("ORG_FILE_NM").toString();
							fileName = itnNew.get("FILE_NM").toString();
							fileSize = itnNew.get("FILE_SIZE").toString();
							
							
//							//SW_FILE의 경우 다운로드 가능 추가=대기중
//							String extension = FilenameUtils.getExtension(CfileName);
//							if(extension.equals("zip") || extension.equals("exe") || extension.equals("zif")) {
//								itnNew.put("PRINT_YN", "N");
//							}
							
							
							//SW에 묶여서 동작
							itnNew = getItnNew(itnNew, quickList);
							itnNew.put("FILE_NO", fileNo);
							itnNew.put("FILE_PATH_NM", filePathName);
							itnNew.put("ORG_FILE_NM", orgFileName);
							itnNew.put("FILE_NM", fileName);
							itnNew.put("FILE_SIZE", fileSize);
							itnNew.put("USE_START_YMD", DateUtil.getToday("yyyyMMdd"));
							itnNew.put("USE_END_YMD", DateUtil.getAddMonth(6, "yyyyMMdd"));
							dao.insertDocsRequestFile(itnNew);
							break;
							default:
								break;
						}
					}

					/* 기존에 발생된 Error가 해결되면 해당 Error log 제거
					if(success && errorCount > 0) {
						dao.deleteError(objectType, itnNew);
						errorCount = dao.selectErrorCount();
					}
					*/

					log.debug("TRANFER RESULT : " + success);
   					dao.updateMovedData(objectType, itnNew, success);
				}

				Iterator<String> keys = distributeMap.keySet().iterator();

				while(keys.hasNext()) {
					String key = keys.next();
					List<Map<String, Object>> list = distributeMap.get(key);
					String requestNo = null;
					
					for(int i=0; i<list.size(); i++) {
						Map<String, Object> tmp = list.get(i);
						if(i==0) {
							dao.insertDocsRequest(tmp);
							requestNo = tmp.get("REQUEST_NO").toString();
							dao.insertDocsRequestDeploy(tmp);
							dao.insertDocsRequestDetail(tmp);
						}
						dao.insertDocsRequestMapping(tmp);
						
						
						if(null == tmp.get("FILE_NO")) {
							tmp.put("FILE_NO", 1);
						}
						
						switch (objectType) {
						case DRAWING:
						case PRODUCT_DOCUMENT_FILE:
						case PRODUCT_SW_FILE:
							dao.insertDocsRequestFile(tmp);
							break;
						default:
							break;
						}
					}
				}

				//해당 UNIT만큼 Data 처리후 Commit
				transactionManager.commit(transactionStatus);
			}catch(Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				transactionManager.rollback(transactionStatus);
			}
			//Migration시 WAS 죽는 문제가 있다 해서, Break 처리.
			//UNIT만큼만 처리하고 다음 Table로 넘어가려면, break 주석 제거.
			//대상 Table의 모든 신규 Data를 처리한후 다음으로 넘어가려면, break 주석 처리.
			//break;
		}
	}

	private Map<String, Object> getItnNew(Map<String, Object> param, List<Map<String, Object>> list) {
		Map<String, Object> map = null;
		for(Map<String, Object> tmp : list) {
			if(tmp.get("CN_SERIAL").equals(param.get("CN_SERIAL")) && tmp.get("OBJECT_ID").equals(param.get("OBJECT_ID"))) {
				map = tmp;
			}
		}
		return map;
	}

	
	
}
