package kr.esob.fdms.commonlogic.FromAdapToItn;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.seq.SeqDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class FromAdapToItnService implements CommonService{

	//@Autowired private PlatformTransactionManager transactionManager;

	@Inject
	FromAdapToItnDao dao;

	@Inject
	SeqDao seqDao;

	@Inject
	private DocsMailService mailService;

	List<Map<String, Object>> quickListTmp = new ArrayList<Map<String,Object>>();
	List<Map<String, Object>> quickList = new ArrayList<Map<String,Object>>();

	Map<String, Object> quickMap = new HashMap<String, Object>();

	int insertResult = 0;
    @Autowired
    private DocsMailService docsMailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return null;//dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return 0; //dao.selectListCount(param);
	}

//	public void transferAdapToItn() throws Exception {
//		try {
//
//			// config table에서, 대상파일 경로 조희
//			List<Map<String,Object>> dbConfig = dao.selectDbConfig();
//			String adapDocFilePath="";
//			String adapRepoPath="";
//			String adapViewerPath="";
//			String sourceDrive = "";
//			final Integer UNIT = 10000; // UNIT 갯수만큼 처리하고 Commit
//
//			// 디비 값들을 본다.
//			for(Map<String,Object> config : dbConfig) {
//				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_ORG_FILE_PATH")) {
//					adapDocFilePath = config.get("SYSTEM_CONFIG_VALUE").toString();
//				}
//				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_REPO_PATH")) {
//					adapRepoPath = config.get("SYSTEM_CONFIG_VALUE").toString();
//				}
//				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_VIEWER_PATH")) {
//					adapViewerPath = config.get("SYSTEM_CONFIG_VALUE").toString();
//				}
//			}
//
//			// db에서 경로들을 다 가져온다.
//			List<Map<String,Object>> pathList = dao.selectDbConfig();
//			// 가져올 SYSTEM_CONFIG_CD 값들
//			List<String> path = new ArrayList<>();
//			path.add("3D_FILE_PATH");
//			path.add("2D_FILE_PATH");
//			path.add("SW_PATH");
//			path.add("DOCUMENT_PATH");
//			path.add("PRODUCTION_PATH");
//
//			quickListTmp.clear();
//			for(Map<String,Object> config : pathList) {
//				String tempKey = config.get("SYSTEM_CONFIG_CD").toString();
//				String tempPath = config.get("SYSTEM_CONFIG_VALUE").toString();
//				if(path.contains(tempKey)) {
//					getFilesInDir(tempPath,tempPath);
//				}
//			}
//			//System.out.println("adapDocFilePath = " + adapDocFilePath);
//
//			//초기파일목록을 조회해서 List를 비우고 추가
//			// quickListTmp.clear();
//			getFilesInDir(adapDocFilePath,adapDocFilePath);
//			//quickListTmp.forEach(System.out::println);
//
//			//List의 내용을 임시테이블(파일경로, 파일사이즈만 있음)에 입력
//			for(Map<String, Object> tempNew : quickListTmp) {
//
//				try {
//					log.info("파일 데이터들 -> {}", tempNew);
//					dao.insertTempFilePath(ADAP_TYPE.TEMP, tempNew);
//				} catch(Exception e) {
//					log.error(e.getMessage());
//					e.printStackTrace();
//				}
//			}
//
//			//db 임시 테이블에서 기존 REPO 테이블과 비교해서 업데이트 대상을 추가하고 파일이동 후 temp삭제
//			try {
//				boolean success = false;
//
//				//db repo 등록
//				dao.insertRepo(ADAP_TYPE.REPO, "Repo insert");
//
//				//ㅠㅠㅠㅠㅠㅠㅠㅠ
//				//(파일 repo db의 new_YN이 Y면 temp에서 repo로 복사하고 N으로 바꿈 )로직변경-> 바로 ADAP로 경로 데이터만 넘기고 파일카피
//				// 디비에만 넣어주면 되므로 주석처리 23.05.25 by koo 같은 주석 있는 라인까지 주석처리 됨.
////				try {
////					List<Map<String, Object>> itnNewList = dao.selectNewDataFromRepo(ADAP_TYPE.REPO, UNIT);
////
////					Integer temp = 0;
////					//itn에 들어온 리스트 중 하나씩 꺼내 정보처리
////					for(Map<String, Object> itnNew : itnNewList) {
////
////						temp++; //log용
////						log.debug("######################### " + temp.toString() + " ####################");
////
////						if(itnNew.get("FILE_PATH_NM") != null && !itnNew.get("FILE_PATH_NM").toString().equals(" ")) {
////							String filePathNm = itnNew.get("FILE_PATH_NM").toString();
////							String orgFileName = itnNew.get("ORG_FILE_NM").toString();
////							String strObjectId = itnNew.get("OBJECT_ID").toString();
////
////							String surceFileFullPath = adapDocFilePath + filePathNm; //원본 파일 Full Path
////							String destFileFullPath = adapViewerPath + filePathNm;  //이동할 장소 Full Path
////
////							//System.out.println("surceFileFullPath = " + surceFileFullPath);
////							//System.out.println("destFileFullPath = " + destFileFullPath);
////
////							try {
////								//파일을 저장할 경로추출 후, 해당 경로가 없으면 경로 생성
////								Integer lastIndex = destFileFullPath.lastIndexOf("/");
////								String destDir = (lastIndex > -1) ? destFileFullPath.substring(0,lastIndex+1) : "";
////								File dir = new File(destDir);
////								File tempDestFile = null;
////
////								boolean mkdir = true;
////								if(!dir.exists()) {
////									//Directory가 없으면 Dir 생성
////									mkdir = dir.mkdirs();
////									if(!mkdir) {
////										//Dir 생성 실패
////										log.error("FAIL TO MAKE DIR : " + destDir);
////									}
////								}else {
////									//이미 폴더에 같은 이름의 파일이 있을경우 {index} 를 파일명에 추가 시킴.
////									tempDestFile = new File(destFileFullPath);
////									/* 오버라이트함 */
////									if(tempDestFile.exists()) {
////										Integer fileIndex = 0;
////
////										//파일명과 확장자 분리.
////										Integer lastPoint = orgFileName.lastIndexOf(".");
////										String fileName = (lastPoint > -1) ? orgFileName.substring(0,lastPoint) : orgFileName;
////										String extName = (lastPoint > -1) ? orgFileName.substring(lastPoint,orgFileName.length()) : "";
////										do {
////											fileIndex++;
////											String changedFileName = fileName + '{' + fileIndex + '}' + extName;
////											destFileFullPath = destDir + changedFileName;
////
////											tempDestFile = new File(destFileFullPath);
////										}while(tempDestFile.exists());
////									}
////									/* 오버라이트함 */
////								}
////
////								//목적지로 파일 이동, 파일 이동후에 DB에 추가
////								/* File 이동에서 복사로 임시 변경. */
////								//File file = new File(surceFileFullPath); //원본파일
////								//if(file.renameTo(new File(destFileFullPath))) {
////
////								//}
////								/**/
////
////								if(mkdir && FileUtil.copyFile(surceFileFullPath, destFileFullPath)) {
////									//File 이동 성공
////									itnNew.put("FILE_PATH_NM", destFileFullPath.replace(sourceDrive, "")); //파일이 복사된 새로운 경로로 Table에 입력이 되어야 함.
////
////									//카피후 NEW_YN N으로 Update
////									//Map<String, Object> map = new HashMap<String, Object>();
////									//map.clear();
////									//map.put("OBEJCT_ID",strObjectId);
////
////									System.out.println("destFileFullPath=" + destFileFullPath);
////									//System.out.println("strObjectId=" + strObjectId);
////									//dao.updateRepo(ADAP_TYPE.REPO2, strObjectId);
////									dao.updateRepo(ADAP_TYPE.REPO2, itnNew);
////
////									/*bbbb
////									//if(exist) {
////									//	//Data가 존재하므로 Update
////									//	dao.updateDocs(objectType, itnNew);
////									//}else {
////									//	//Data가 없으므로 Insert.
////									//	dao.insertDataToDocs(objectType,itnNew);
////									//}
////									bbbb */
////								}else {
////									//File 이동 실패
////									//dao.insertError(objectType,itnNew , "FAIL_TO_MOVE_FILE");
////									log.error("FAIL TO COPY : " + surceFileFullPath);
////									success = false;
////								}
////							}catch(Exception e) {
////								//dao.insertError(objectType,itnNew , e.getMessage());
////								e.printStackTrace();
////								success = false;
////							}
////						} else {
////
////							log.error("FILE PATH IS NULL");
////						}
////
////
////
////					}
////
////				}catch(Exception e) {
////					//dao.insertError(objectType,itnNew , e.getMessage());
////					e.printStackTrace();
////					success = false;
////				}
//				// 디비에만 넣어주면 되므로 주석처리 23.05.25 by koo 같은 주석 있는 라인까지 주석처리 됨.
//
//
//			} catch(Exception e) {
//				log.error(e.getMessage());
//				e.printStackTrace();
//			}
//
//			//db temp 삭제
//			// dao.deleteTemp(ADAP_TYPE.TEMP, "Temp delete");
//
//			//ADAP 데이터 생성 - REPO에서 REPO_TO_ADAP가 N이면 처리하고 Y로 업데이트
//			try {
//
//				log.info("음 123");
//				List<Map<String, Object>> itnRepoList = dao.selectRepoDataFromRepo(ADAP_TYPE.REPO);
//				dao.deleteTemp(ADAP_TYPE.TEMP, "Temp delete");
//				itnRepoList.forEach((e) -> {
//					log.info("e -> {}", e);
//				});
//
//				for(Map<String, Object> itnNew : itnRepoList) {
//
//					//dao.insertAdapDocumnet(ADAP_TYPE.ADAP_DOCUMENT, itnNew.toString());
//					//dao.insertAdapDocumnetFile(ADAP_TYPE.ADAP_DOCUMENT_FILE, itnNew.toString());
//					//dao.insertAdapDocumnetMember(ADAP_TYPE.DOCUMENT_MEMBER, itnNew);
//
//					//Repo_to_adap Y로 업데이트
//					dao.updateRepoToAdap(ADAP_TYPE.REPO, itnNew);
//				}
//
//
//				//ADAP 데이터 생성 - REPO에서 REPO_TO_ADAP가 N이면 처리하고 Y로 업데이트, 도면 문서 구분
//				dao.insertAdapDocumnet(ADAP_TYPE.ADAP_DOCUMENT, "document");
//				dao.insertAdapDocumnetFile(ADAP_TYPE.ADAP_DOCUMENT_FILE, "document file");
//				//dao.insertAdapDocumnetMember(ADAP_TYPE.ADAP_DOCUMENT_MEMBER, "document member");
//				dao.insertAdapDrawing(ADAP_TYPE.ADAP_DRAWING, "drawing");
//				//dao.insertAdapDrawingMember(ADAP_TYPE.ADAP_DRAWING_MEMBER, "drawing member");
//
//
//				//ADAP 데이터를 DOCS로 이동하고 MOVED_TO_ITN 업데이트
//
//				try {
//					dao.insertDocsDocumnet(ADAP_TYPE.DOCS_DOCUMENT, "document");
//				} catch (Exception e) {
//					log.error(e.getMessage());
//				}
//				try{
//					dao.updateAdapRepo(ADAP_TYPE.DOCS_DOCUMENT, "");
//				}catch (Exception e){
//					log.error(e.getMessage());
//				}
//
//				try {
//					dao.insertDocsDocumnetFile(ADAP_TYPE.DOCS_DOCUMENT_FILE, "document file");
//				} catch (Exception e) {
//					log.error(e.getMessage());
//				}
//
//
//				try {
//					dao.insertDocsDrawing(ADAP_TYPE.DOCS_DRAWING, "drawing");
//				} catch (Exception e) {
//					log.error(e.getMessage());
//				}
//
//				dao.updateAdapRepo(ADAP_TYPE.REPO, "update ");
//			} catch(Exception e) {
//				log.error(e.getMessage());
//				e.printStackTrace();
//			}
//
//
//			//ADAP에서 DOCS로 데이터 전달
//
//
//			//ADAP에서 DOCS로 파일 복사 - Repo에서 동기화 되어있음
//
//
//		} catch(Exception e) {
//			log.error(e.getMessage());
//			e.printStackTrace();
//		}
//	}


	// 2023. 07. 25 기범 주석처리 ( 등록 기능만 사용. 스케쥴러는 일단 주석처리 )
//	public void fileAutoInsertSchedule() {
//		/**
//		 * 로직은 간단하다.
//		 * 1. temp에 데이터를 넣는다.
//		 * 2. temp에 들어가면 repo로 자동으로 넘어간다(혹은 넘어가게 만든다.)
//		 * 3. repo에 있는게 adap_다른 테이블에 들어간다.
//		 * 4. adap_다른 테이블 -> docs_다른테이블 넣어준다.
//		 * 5. repo의 테이블에 있는 repo_to_adap -> N -> Y 로 바꾸어준다.
//		 * temp를 거치지 않고 바로 repo에 넣고 분류해주는게 맞는듯
//		 */
//
//		// db에서 경로들을 다 가져온다.
//		List<Map<String,Object>> pathList = dao.selectDbConfig();
//		// 가져올 SYSTEM_CONFIG_CD 값들
//		List<String> path = new ArrayList<>();
//		path.add("3D_FILE_PATH");
//		path.add("2D_FILE_PATH");
//		path.add("SW_PATH");
//		path.add("DOCUMENT_PATH");
//		path.add("PRODUCTION_PATH");
//
//
//		for(Map<String,Object> config : pathList) {
//			String tempKey = config.get("SYSTEM_CONFIG_CD").toString();
//			String tempPath = config.get("SYSTEM_CONFIG_VALUE").toString();
//			if(path.contains(tempKey)) {
//				// 포함되어 있다면
//				log.info("path -> {} : {}", tempKey, tempPath);
//
//				quickListTmp.clear();
//				getFilesInDir(tempPath,tempPath);
//				//quickListTmp.forEach(System.out::println);
//
//				//List의 내용을 임시테이블(파일경로, 파일사이즈만 있음)에 입력
//				for(Map<String, Object> tempNew : quickListTmp) {
//
//					try {
//						log.info("임시 파일 데이터들 -> {}", tempNew);
//						// 1번 시행
//						List<Map<String, Object>> list = dao.selectTempFile(tempNew);
//						if(list.isEmpty()) {
//							dao.insertTempFilePath(ADAP_TYPE.TEMP, tempNew);
//						}
//					} catch(Exception e) {
//						log.error("에러 {}",e.getMessage());
//						// e.printStackTrace();
//					}
//				}
//				// 2번 시행
//				dao.insertRepo(ADAP_TYPE.REPO, "Repo insert");
//
//				// 각 폴더에 있는것들은 다~ 디비에 알맞게 들어가기만 하면 됨.
//				// 디렉토리 객체 생성
//				File directory = new File(tempPath);
//
//				// 디렉토리가 존재하는지 확인
//				if (directory.exists() && directory.isDirectory()) {
//					// 디렉토리 내의 파일들을 가져오기
//					File[] files = directory.listFiles();
//
//					// 파일들 출력
//					// 3번
//					for (File file : files) {
//						String str = RandomStringGenerator.generateRandomString(32);
//						Map<String, Object> map = new HashMap();
////						if(tempKey.equals("2D_FILE_PATH")){
////							//dao.insertAdapDrawing(ADAP_TYPE.ADAP_DRAWING, "drawing");
////							//dao.insertAdapDrawing(ADAP_TYPE.DOCS_DRAWING, "drawing");
////						} else
//							if(tempKey.equals("2D_FILE_PATH") || tempKey.equals("3D_FILE_PATH")){
//							//dao.insertAdapDrawing(ADAP_TYPE.ADAP_DRAWING, "drawing");
//							//dao.insertAdapDrawing(ADAP_TYPE.DOCS_DRAWING, "drawing");
//
//							map.put("FILE_PATH_NM", file.getPath());
//							map.put("ORG_FILE_NM", file.getName());
//							map.put("FILE_NM", file.getName());
//							map.put("FILE_SIZE", file.length());
//							map.put("OBJECT_ID", str);
//
//							Map resultMap = dao.selectDocsDrawing(map);
//							if(resultMap == null || resultMap.isEmpty()){
//								map.put("DRAWING_NO", file.getName());
//								map.put("DRAWING_NM", file.getName());
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_DRAWING_V2, map);
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_DRAWING_FILE_V2, map);
//							}
//
//						} else if(tempKey.equals("DOCUMENT_PATH")){
//							//dao.insertDocsDocumnet(ADAP_TYPE.ADAP_DOCUMENT, "drawing");
//							//dao.insertDocsDocumnet(ADAP_TYPE.ADAP_DOCUMENT_FILE, "drawing");
//							//dao.insertDocsDocumnet(ADAP_TYPE.DOCS_DOCUMENT, "");
//							//dao.insertDocsDocumnet(ADAP_TYPE.DOCS_DOCUMENT_FILE, "");
//
//
//							map.put("FILE_PATH_NM", file.getPath());
//							map.put("ORG_FILE_NM", file.getName());
//							map.put("FILE_NM", file.getName());
//							map.put("FILE_SIZE", file.length());
//							map.put("OBJECT_ID", str);
//
//							Map resultMap = dao.selectDocsDocumentFile(map);
//							if(resultMap == null || resultMap.isEmpty()){
//								map.put("DOCUMENT_NM", file.getName());
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_DOCUMENT_V2, map);
//
//								map.put("FILE_NO", 1);
//								map.put("DISTRIBUTE_TYPE_CD", 1);
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_DOCUMENT_FILE_V2, map);
//							}
//						} else if(tempKey.equals("PRODUCTION_PATH")){
//							// 생산 기술 자료
//							log.info("여기 안해?");
//							map.put("FILE_PATH_NM", file.getPath());
//							map.put("ORG_FILE_NM", file.getName());
//							map.put("FILE_NM", file.getName());
//							map.put("FILE_SIZE", file.length());
//							map.put("OBJECT_ID", str);
//
//							Map resultMap = dao.selectDocsProduction(map);
//							if(resultMap == null || resultMap.isEmpty()){
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_PRODUCT_DOCUMENT, map);
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_PRODUCT_DOCUMENT_FILE, map);
//							}
//							//dao.insertAdapDrawing(ADAP_TYPE.ADAP_DRAWING, "drawing");
//							//dao.insertAdapDrawing(ADAP_TYPE.DOCS_DRAWING, "drawing");
//						} else if(tempKey.equals("SW_PATH")){
//							// temp지만 사실 원리는 hashMap을 던져주는 방식이라 상관 x, 변수명은 나중에 바꿀 예정 23.06.05 by koo
//
//							map.put("FILE_PATH_NM", file.getPath());
//							map.put("ORG_FILE_NM", file.getName());
//							map.put("FILE_NM", file.getName());
//							map.put("FILE_SIZE", file.length());
//							map.put("OBJECT_ID", str);
//							Map resultMap = dao.selectSWFile(map);
//
//							if(resultMap == null || resultMap.isEmpty()){
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_SW, map);
//								dao.insertTempFilePath(ADAP_TYPE.DOCS_SW_FILE, map);
//							}
////							dao.insertDocsDocumnet(ADAP_TYPE.ADAP_SW_, "");
//
//						}
//						System.out.println(file.getName());
//					}
//
//				} else {
//					System.out.println("디렉토리가 존재하지 않습니다.");
//				}
//			}
//
//		}
//		// 4 실행
//		dao.updateRepo(ADAP_TYPE.REPO, "");
//		dao.updateRepo(ADAP_TYPE.DOCS_DRAWING, "");
//		dao.updateRepo(ADAP_TYPE.DOCS_DOCUMENT, "");
//		dao.updateRepo(ADAP_TYPE.DOCS_DOCUMENT_FILE, "");
//
//	}



	//	private final String sharedFolderPath = "\\\\192.168.0.xxx\\cad";  // 공유 폴더 경로
	private String file_path_scheduled = "";  // 공유 폴더 경로

	private Set<String> previousFileSet = new HashSet<>();  // 이전 파일 목록 추적

	@Inject
	PdmDataReader adbService;  // A DB 접근 서비스
	@Inject
	TargetDbWriter bdbService;  // B DB 접근 서비스

	@Transactional(rollbackFor = Exception.class)
	public void fileAutoInsertSchedule() {
		List<Map<String,Object>> dbConfig = dao.selectDbConfig();
		String file_path_2d="";
		String file_path_3d="";
		String file_path_doc="";
		String file_path_sw="";
		String file_path_cp="";
		String file_path_dxf="";


		for(Map<String,Object> config : dbConfig) {
			if(config.get("SYSTEM_CONFIG_CD").equals("2D_FILE_PATH")) {
				file_path_2d = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("3D_FILE_PATH")) {
				file_path_3d = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("DOCUMENT_PATH")) {
				file_path_doc = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("SW_PATH")) {
				file_path_sw = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("PRODUCTION_PATH")) {
				file_path_cp = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("SCHEDULED_FILE_PATH")) {
				file_path_scheduled = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("DXF_PATH")) {
				file_path_dxf = config.get("SYSTEM_CONFIG_VALUE").toString() + "\\";
			}
		}

		try {
			String currentDir = Paths.get("").toAbsolutePath().toString();
			log.info("스케줄러가 실행되는 디렉터리: {}", currentDir);

			log.info("공유 폴더에서 새 파일을 탐색합니다...");
			Set<String> currentFileSet = getCurrentFileSet();	// 공유폴더에 있는 모든 파일의 파일이름을 가져옴(중복 제거)

			// 새로 추가된 파일 찾기
			// currentFileSet.removeAll(previousFileSet); // 동일한 파일명의 자료를 조회하지 않는 코드 주석처리

			System.out.println("Current File Set:");
			for (String file : currentFileSet) {
				System.out.println(file);
			}

			if (!currentFileSet.isEmpty()) {
				for (String fileName : currentFileSet) {
					log.info("새로운 파일 발견: {}", fileName);

					File file = new File(file_path_scheduled, fileName);
					Map<String, Object> data = new HashMap<>();

					// A DB에서 데이터 조회
					adbService.getDataFromADB(data, fileName);

					if (!data.isEmpty()) {
						data.put("file_size", file.length());
						// 이레산업에서 distribution_type 컬럼이 없다면
						data.putIfAbsent("distribution_type", "도면/공정서");

						StringBuilder sb = new StringBuilder("-------data 출력-------\n");

						for (Map.Entry<String, Object> entry : data.entrySet()) {
							sb.append("  Key: ")
									.append(entry.getKey())
									.append(", Value: ")
									.append(entry.getValue())
									.append("\n");
						}
						log.info(sb.toString());

						// object_id 생성하는 임시 메서드 호출@
						String objectId = generateObjectId();
						data.put("object_id", objectId);
						String objectNo = "";

						insertResult = insertTargetDb(data, file_path_2d, file_path_3d, file_path_doc, file_path_sw, file_path_cp, file_path_dxf);

						// insertResult가 true일 때만 파일을 이동하고 삭제하도록 변경
						if (insertResult > 0) {
							log.info("파일 {} 관련 데이터가 B DB에 저장되었습니다.", fileName);

							// 파일 이동 후 삭제 (objectId로 이름 변경)
							String srcPath = file_path_scheduled + "\\" + fileName;

							String distributionType = (String) data.get("distribution_type");
							log.info("현재 문서 타입은 {}입니다.", distributionType);

							String check3d = (String) data.get("check_3dfile");
							boolean isSuccess = true;

							switch (distributionType) {
								case "도면/공정서":
									if (check3d.equals("Y")) {
										isSuccess = moveFileToDestination(srcPath, file_path_3d, objectId);
									} else {
										isSuccess = moveFileToDestination(srcPath, file_path_2d, objectId);
									}
									break;
								case "문서":
									isSuccess = moveFileToDestination(srcPath, file_path_doc, objectId);
									break;
								case "SW":
									isSuccess = moveFileToDestination(srcPath, file_path_sw, objectId);
									break;
								case "CP":
								case "CP 도면":
									isSuccess = moveFileToDestination(srcPath, file_path_cp, objectId);
									break;
								case "DXF":
								case "DXF 도면":
									isSuccess = moveFileToDestination(srcPath, file_path_dxf, objectId);
									break;
							}

							if(!isSuccess) {
								int deletedRows = bdbService.deleteDataFromTargetDB(objectId, distributionType);
								if (deletedRows > 0) {
									log.info("파일 이동 중 오류가 발생하여 {}개의 행이 삭제되었습니다.", deletedRows);
								} else {
									log.info("삭제 작업이 실패했습니다.");
								}
							} else{
								objectNo = data.get("drawing_no") != null && !data.get("drawing_no").toString().isEmpty()
										? data.get("drawing_no").toString()
										: data.get("customer_document_no").toString();

								log.info("obejctNo: {}", objectNo);
								List<String> requestNos = bdbService.getPrevRevisionFile(objectNo);
								if (!requestNos.isEmpty()) {
									log.info("revision updated");

									boolean saveRevision = saveRevisionInfo(requestNos, data);
									if(!saveRevision) log.error("리비전 변경 데이터 저장 실패");

//									boolean mailSent = sendMail(requestNos, data);
									boolean mailSent = false;
									if(!mailSent){
										log.error("메일 발송 실패");
									}
								}
								else{
									log.info("신규 데이터가 저장 및 복사되었습니다.");
								}
								removeFile(srcPath);
							}
						} else {
							log.error("데이터베이스에 저장 실패로 인해 파일을 이동하지 않았습니다: {}", fileName);
						}


					} else {
						log.warn("A DB에서 파일 {}에 대한 데이터를 찾을 수 없습니다.", fileName);
					}
				}
			} else {
				log.info("새로운 파일이 없습니다.");
			}

			// 이전 파일 목록 갱신
			previousFileSet = getCurrentFileSet();
		} catch (Exception e) {
			log.error("스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	// 공유 폴더의 현재 파일 목록을 가져오는 메서드
	private Set<String> getCurrentFileSet() {
		File folder = new File(file_path_scheduled);
		log.info("공유 폴더 경로: {}", file_path_scheduled);
		String[] files = folder.list();
		return files != null ? new HashSet<>(Arrays.asList(files)) : new HashSet<>();
	}

	/**
	 * 초기파일목록을 조회
	 * @return
	 */
	public void getFilesInDir(String dirRootPath,String dirPath) {

		log.info("파일 넣는중 -> {}", dirRootPath);
		//System.out.println("dirPath = " + dirPath);
		File dir = new File(dirPath);
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				getFilesInDir(dirRootPath, file.getPath());
			} else {
				// System.out.println("file :  " + file.getPath());
				//System.out.println("file :  " + file.length());

				Map<String, Object> map = new HashMap<String, Object>();
				//map.put(String.valueOf(file), file.length());
				//map.put("i", i);

//				map.put("FILE_PATH_NM", String.valueOf(file).replace(dirRootPath, "").replace("\\", "/"));
				map.put("FILE_PATH_NM", file.getPath().replace("\\", "/"));

				map.put("ORG_FILE_NM", String.valueOf(file).replace(dirPath+"\\", ""));
				map.put("FILE_SIZE", file.length());
				quickListTmp.add(map);
				// quickListTmp.add(i,map);
				//quickMap.put(String.valueOf(file), file.length());

			}
		}
	}


	/**
	 * 파일 복사
	 * @return
	 */
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


	//임시로 사용하는 object_id 생성 메서드
	private String generateObjectId(){
		String pk = UUID.randomUUID().toString();
		return pk;
	}

	// distributionType 확인 후 알맞은 테이블에 데이터 생성
	private int insertTargetDb(Map<String, Object> data, String file_path_2d, String file_path_3d,
							   String file_path_doc, String file_path_sw, String file_path_cp, String file_path_dxf) {
		String distributionType = (String)data.get("distribution_type");
		String orgFileNm = data.get("org_file_nm") != null ? data.get("org_file_nm").toString() : "";
		String objectId = data.get("object_id") != null ? data.get("object_id").toString() : "";
		String extension = getFileExtension(orgFileNm);
		String filePathNm = "" ;


		if (distributionType.equals("도면/공정서")) {
			if(data.get("check_3dfile").toString().equals("Y")) { filePathNm = file_path_3d + objectId + extension; }
			else if(data.get("check_3dfile").toString().equals("N")) { filePathNm = file_path_2d + objectId + extension; }
			else{ filePathNm = file_path_2d + objectId + extension;}
		}
		else if (distributionType.equals("문서")) { filePathNm = file_path_doc + objectId+ extension;}
		else if (distributionType.equals("CP 도면")) { filePathNm =  file_path_cp + objectId + extension;}
		else if (distributionType.equals("CP")) {  filePathNm =  file_path_cp + objectId + extension;}
		else if (distributionType.equals("생산기술자료")) {  filePathNm =  file_path_cp + objectId + extension;}
		else if (distributionType.equals("SW")) { filePathNm = file_path_sw + objectId + extension;}
		else if (distributionType.equals("DXF 도면")) { filePathNm = file_path_dxf + objectId + extension;}
		else if (distributionType.equals("DXF")) { filePathNm = file_path_dxf + objectId + extension;}
		else { log.error("filePathNm 값 할당되지않음.");}

		String fileNm = orgFileNm.replace(extension, "");

		data.put("file_path_nm", filePathNm);
		data.put("file_nm", fileNm);

		return bdbService.insertDataToBDB(data, distributionType);
	}

	private boolean moveFileToDestination(String srcPath, String destDir, String objectId) throws IOException {
		File srcFile = new File(srcPath);
		File destFile = new File(destDir, objectId + getFileExtension(srcFile.getName()));  // objectId로 이름 변경

		boolean isSuccess = true;

		// 대상 경로가 존재하지 않으면 생성
		if (!new File(destDir).exists()) {
			new File(destDir).mkdirs();
		}

		// 파일 복사
		if (copyFile(srcPath, destFile.getAbsolutePath())) {
			log.info("파일 {}이 {}로 복사되었습니다.", srcPath, destFile.getAbsolutePath());
		} else {
			log.error("파일 복사에 실패했습니다: {}", srcPath);
			isSuccess = false;
		}
		return isSuccess;
	}

	private boolean removeFile(String srcPath) throws IOException {
		File srcFile = new File(srcPath);
		boolean isSuccess = true;

		if (srcFile.delete()) {
			log.info("원본 파일 {}이 삭제되었습니다.", srcPath);
		} else {
			log.error("원본 파일 {} 삭제에 실패했습니다.", srcPath);
			isSuccess = false;
		}
		return isSuccess;
	}

	// 파일 확장자 추출 메서드
	private String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		return (index > 0) ? fileName.substring(index) : "";  // 확장자가 없으면 빈 문자열 반환
	}

	private boolean saveRevisionInfo(List<String> requestNos, Map<String, Object> data){
		log.info("saveRevisionInfo is called");
		boolean result = false;
		int queryResult = 0;

		// 한 명 이상의 외부사용자가 배포 요청했던 자료라면 각 외부사용자마다 개별적으로 나누어 저장
		for(String requestNo : requestNos){
			MailInfoVO mailParam = mailService.selectDeployUserInfo(requestNo);
			if (data != null && mailParam != null && data.get("to_mail") != null && mailParam.getToMail() != null &&
					mailParam.getToMail().equals(data.get("to_mail"))) {
				System.out.println("Skipping requestNo: " + requestNo + " as outside user is already processed.");
				continue;
			}

			if(data.get("to_mail")==null){System.out.println("data.get(to_mail) is null");}
			if(mailParam.getToMail()==null){System.out.println("mailParam.getToMail() is null");}

			Long mailId = getPk();

			data.put("mail_id", mailId);
			data.put("to_mail", mailParam.getToMail());
			data.put("user_nm", mailParam.getToUserId());

			queryResult += bdbService.insertRevisionData(data);
		}
		log.info("queryResult: {}", queryResult);
		log.info("requestNos.size(): {}", requestNos.size());

		if(queryResult == requestNos.size()) {
			result = true;
		}

		return result;
	}

	public void mergeMails(){

		List<MailInfoVO> mails = new ArrayList<>();

		// 1. docs_mail_revision 테이블에서 status가 "F"인 모든 데이터 조회
		// 2. to_mail 컬럼끼리 매핑
		Map<String, List<Map<String, Object>>> groupedData = bdbService.getRevisionData();

		// 3. 합친 본문 생성 (key: to_mail, value: 본문)
		if(!groupedData.isEmpty()) {
//			mails = generateMailContent(groupedData);
			mails = excuteMergeMails(groupedData);

			for(MailInfoVO mailInfoVO : mails){
				boolean insertResult = docsMailService.insertDocsMail(mailInfoVO);	// 4. 합친 데이터는 docs_mail 테이블로 이동
				log.info("insertResult to DOCS_MAIL is: {}", insertResult);
			}

			if(!changeRevisionStatus(mails)){
				log.error("Failed to change revision status");
			}
		}
		ResultVO resultVo = mailService.sendScheduledMails();
		log.info("resultVo.isSuccess(): {}", resultVo.isSuccess());
	}

	public List<MailInfoVO> excuteMergeMails(Map<String, List<Map<String, Object>>> groupedData) {
		List<MailInfoVO> mails = new ArrayList<>();

		for (Map.Entry<String, List<Map<String, Object>>> entry : groupedData.entrySet()) {
			String toMail = entry.getKey();
			List<Map<String, Object>> drawingList = entry.getValue();
			MailInfoVO mailInfoVO = new MailInfoVO();
			StringBuilder mailIds = new StringBuilder();

			List<String> referenceKeys = new ArrayList<>();

			for (int i = 0; i < drawingList.size(); i++) {
				Map<String, Object> drawing = drawingList.get(i);

				if (mailIds.length() > 0) {
					mailIds.append(",");
				}
				mailIds.append(drawing.get("mail_id"));
				mailInfoVO.setToUserId(drawing.get("user_nm").toString());
				referenceKeys.add(drawing.get("mail_id").toString());
			}

			StringBuilder referenceKeysBuilder = new StringBuilder();
			for (int i = 0; i < referenceKeys.size(); i++) {
				if (i > 0) {
					referenceKeysBuilder.append(",");
				}
				referenceKeysBuilder.append(referenceKeys.get(i));
			}

			DocsMailEnum mailEnum = DocsMailEnum.REVISION_UPDATED;

			mailInfoVO.setToMail(toMail);
			mailInfoVO.setContent(mailEnum.getContent());
			mailInfoVO.setMailId(mailIds.toString());
			mailInfoVO.setReferenceKeys(referenceKeysBuilder.toString());

			mails.add(mailInfoVO);
		}
		return mails;
	}

	private boolean changeRevisionStatus(List<MailInfoVO> revisionMails) {

		int totalChangRows = 0;

		for(MailInfoVO mailInfoVO : revisionMails){
			String[] mailIds = mailInfoVO.getToMail().split(",");
			totalChangRows += mailIds.length;
		}

		int result = bdbService.changeRevisionData(revisionMails);

        return totalChangRows == result;

	}

	private boolean sendMail(List<String> requestNos, Map<String, Object> data){
		boolean result = false;
		for(String requestNo : requestNos){
			MailInfoVO mailParam = mailService.selectDeployUserInfo(requestNo);
			ResultVO resultVO = mailService.sendDocsMail(mailParam);
			if(resultVO.isSuccess()){
				result = true;
				return result;
			} else {
				log.info("failed to sendMail");
				return result;
			}
		}
		return result;
	}

	private Long getPk(){
		Long seq = null;
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put("tableNm", "DOCS_MAIL_REVISION");
		map.put("sessionYn", "N");

		seq = seqDao.getSeq(map);
		return seq;
	}
}