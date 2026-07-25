package kr.esob.fdms.commonlogic.viewer;


import kr.esob.fdms.commonlogic.fileapi.FileApiClient;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.message.CommonMessageContainer;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.inside.unregisted.request.UnregisterRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.StoragePathUtils;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CommonViewerService implements CommonService{

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	CommonViewerDao dao;

	@Inject
	DocPdfLinkRequestDao pdao;

	@Inject
	CommonMessageContainer msg;

	@Inject
	CommonRequestDao commonRequestDao;

	@Inject
	UnregisterRequestDao unregisterRequestDao;

	@Inject
	SecurityAclService securityAclService;

	@Inject
	PrintAuditService printAuditService;

	@Inject
	ViewerTicketService viewerTicketService;

	private final FileApiClient fileApiClient = new FileApiClient();

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
	private static final String EXCEPTION_EXT[] = {"zip", "exe"};
	static final String MERGE_PRINT_SECURITY_REASON =
			"MERGE_PRINT_DISABLED_TICKETED_PDF_ONLY";
	private static final String MERGE_PRINT_SECURITY_MESSAGE =
			"Merged printing is disabled because a multi-resource "
			+ "ticket-backed PDF cannot be securely authorized.";
	static final String PRINT_CALLBACK_REQUIRED_REASON =
			"PRINT_RESULT_CALLBACK_REQUIRED";
	private static final String PRINT_CALLBACK_REQUIRED_MESSAGE =
			"출력 성공 결과를 검증하는 뷰어 연계가 완료될 때까지 출력 기능을 사용할 수 없습니다.";

	private CommonViewerVO getFileInfo(CommonViewerParam param) {
		CommonViewerVO tempFileInfo = new CommonViewerVO();
		if("OBJECT".equals(param.getRequestType())) {                //도면,문서,SW 아이템
			tempFileInfo = dao.getObjectFileInfo(param);
		}else if("UNREG".equals(param.getRequestType())) {            //미등록자료
			tempFileInfo = dao.getUnregFileInfo(param);
		}else if("PRODUCT".equals(param.getRequestType())) {        //생산기술자료
			if(null == param.getRequestNo() || "".equals(param.getRequestNo())) {
				tempFileInfo = dao.getProductFileInfo(param);
			} else {
				tempFileInfo = dao.getFileInfo(param);
			}
		}else {                                                        //배포 자료 파일 (DISTRIBUTION)
			tempFileInfo = dao.getFileInfo(param);
		}

		return tempFileInfo;
	}

//	public String getFilePath(CommonViewerParam param) {
//		String filePath = "";
//		List<CommonViewerVO> fileList = new ArrayList<CommonViewerVO>();
//		if("OBJECT".equals(param.getRequestType())) {				//도면,문서,SW 아이템
//			fileList = dao.selectObjectFileInfo(param);
//		}else if(param.getRequestType().startsWith("UNREG")) {			//미등록자료
//			fileList = dao.selectUnregFileInfo(param);
//		}else if("PRODUCT".equals(param.getRequestType())) {			//생산기술자료
//			param.setObjectType(param.list.get(0).getObjectType());
//			fileList = dao.selectProductFileInfo(param);
//		}else {														//배포 자료 파일
////			fileList = dao.selectFileInfo(param);
//		}
//		for(CommonViewerVO tempVo : fileList) {
//			//파일PATH 구분자가 다른경우 통일을 위한 작업
//			if(tempVo.getFilePath().contains("/")) {
//				tempVo.setFilePath(tempVo.getFilePath().replaceAll("/", "\\\\"));
//			}
//			if("".equals(filePath)) {
//				filePath = "" + tempVo.getFilePath().substring(0,tempVo.getFilePath().lastIndexOf("\\")+1) + tempVo.getFileOrgNm();
//			}else {
//				filePath += "|" + tempVo.getFilePath().substring(0,tempVo.getFilePath().lastIndexOf("\\")+1) + tempVo.getFileOrgNm();
//			}
//		}
//		filePath = filePath.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
//		return filePath;
//	}


	@SuppressWarnings("rawtypes")
	@Override
	public List<CommonViewerVO> selectList(Object object) {
		CommonViewerParam param = (CommonViewerParam) object;
		bindActorAndRequire(param, SecurityAclService.DETAIL);
		List<CommonViewerVO> fileList = new ArrayList<CommonViewerVO>();
		fileList = dao.selectFileList(param);
		return fileList;
	}


	@Override
	public int selectListCount(Object obj) {
		return 0;
	}

//	23.07.06 (yskim) Add check destroyCD value.
public boolean getDestroyStatus(CommonViewerParam param) throws ParseException, UnsupportedEncodingException {
	bindActorAndRequire(param, SecurityAclService.DETAIL);

	CommonViewerVO tempFileInfo = dao.getFileInfoForDestroyStatus(param);
	String tempFileInfo_printHistory = dao.getFileInfoForDestroyStatus_printHistory(param);
	// 프린트 히스토리의 VO에 데이터 담으면 됨. dao로 확인할때, 출력이력 보여주는 db확인 하면 됨.
	// 거기서 폐기요청자 들어 있으면 저 아래 식에 넣어두는거


//		임시방편으로 처리해둠
	if (tempFileInfo == null) {
		return false;
	}


	if ("1".equals(tempFileInfo.getDestroyStatusCd())
			|| "2".equals(tempFileInfo.getDestroyStatusCd())
			|| "3".equals(tempFileInfo.getDestroyStatusCd())) {

		return true;
	} else {

		return false;
	}
}

	public boolean getDestroyStatus_printHistory(CommonViewerParam param) throws ParseException, UnsupportedEncodingException {
		// This lookup is part of the VIEW pre-check. Requiring PRINT here made a
		// view-only user fail before the viewer was opened.
		bindActorAndRequire(param, SecurityAclService.DETAIL);

		String tempFileInfo_printHistory = dao.getFileInfoForDestroyStatus_printHistory(param);
		// 프린트 히스토리의 VO에 데이터 담으면 됨. dao로 확인할때, 출력이력 보여주는 db확인 하면 됨.
		// 거기서 폐기요청자 들어 있으면 저 아래 식에 넣어두는거
//		임시방편으로 처리해둠
		if (tempFileInfo_printHistory == null) {
			return false;
		}


		if (tempFileInfo_printHistory != null) {

			return true;
		} else {

			return false;
		}
	}










	public CommonViewerVO getViewFilePath(CommonViewerParam param) throws ParseException, UnsupportedEncodingException {
		bindActorAndRequire(param, SecurityAclService.VIEW);
		CommonViewerVO result = new CommonViewerVO();
		result.setSuccess(false);
		//파일 정보 조회
		CommonViewerVO tempFileInfo = getFileInfo(param);
		result.setRequestType(param.getRequestType());

		//파일 Copy
//		File orgFile = new File(SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + tempFileInfo.getFilePath());

		if( null==tempFileInfo ) {            //파일이 없을경우
			log.debug("[VIEWER] file metadata not found");
			result.setSuccess(false);
		} else if(tempFileInfo.getDestroyStatusCd() != null && ("1".equals(tempFileInfo.getDestroyStatusCd()) || "2".equals(tempFileInfo.getDestroyStatusCd()) || "3".equals(tempFileInfo.getDestroyStatusCd()))) {
			// 1: 폐기중, 2: 폐기요청, 3: 폐기승인
			// 폐기상태일 경우 출력 불가
			result.setSuccess(false);
			result.setFailType("DESTROY");
			result.setFailReason("폐기상태인 자료는 VIEW 할 수 없습니다.");
		} else {
			String ext = tempFileInfo.getFileOrgNm().substring(tempFileInfo.getFileOrgNm().lastIndexOf(".")+1, tempFileInfo.getFileOrgNm().length());
			for(String exceptionExt : EXCEPTION_EXT) {
				if(ext.equals(exceptionExt)) {
					result.setSuccess(false);
					result.setFailType("NO_SUPPORT_EXT");
					result.setFailReason(exceptionExt);
					return result;
				}
			}
//			String tempPath = SystemConfig.getSystemConfigValue("VIEWER_TEMP_PATH").replace("$", "");
//			String tempFileNm = tempFileInfo.getFileNm() + "." + ext;
//			String copyPath = tempPath + tempFileNm;
//			File temp = new File(tempPath);
//			if(temp.isDirectory()) {
//				temp.mkdir();
//			}
			String orgPath = "";
			String orgPathOut = "";
			boolean fileApiSource = "SW".equals(param.getObjectType()) && isFileApiPath(tempFileInfo.getFilePath());
			if (fileApiSource) {
				log.debug("[FILE_API_VIEWER] source detected");
				orgPath = cacheFileApiFileForViewer(tempFileInfo.getFilePath(), tempFileInfo.getFileOrgNm());
				orgPathOut = orgPath;
			} else if( ("UNREG".equals(param.getRequestType())) || ("UNREG_DISTRIBUTION".equals(param.getRequestType())) ) {
				orgPath = StoragePathUtils.toPath(tempFileInfo.getFilePath()).toString();
			}else {
				orgPath = StoragePathUtils.resolve(
						SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH"),
						tempFileInfo.getFilePath()).toString();
				orgPathOut = tempFileInfo.getFilePath();
//				orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") +  tempFileInfo.getFileNm();
			}


//			if(copyFile(orgPath, copyPath)) {
//				String rtnFilePath = "";
//				if( ("".equals(SystemConfig.getSystemConfigValue("VIEWER_PATH"))) || (null == SystemConfig.getSystemConfigValue("VIEWER_PATH") ) ) {
//					rtnFilePath = SystemConfig.getSystemConfigValue("VIEWER_TEMP_PATH") + tempFileNm;
//				}else {
//					rtnFilePath = SystemConfig.getSystemConfigValue("VIEWER_PATH") + tempFileNm;
//				}
//				rtnFilePath = rtnFilePath.replaceAll("/", "\\\\\\\\");
//				orgPath = orgPath.replaceAll("/", "\\\\\\\\");
//				orgPath = orgPath.replaceAll("/", "\\\\");

//				String sViewerPath = URLEncoder.encode(SystemConfig.getSystemConfigValue("VIEWER_URL") + orgPath);

			result.setAuthLevel(param.getSessionUser().getAuthLevel());

			String sViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE");
			String oViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE");

			if (fileApiSource) {
				String viewerTicket = viewerTicketService.issue(param, new File(orgPath).getName());
				String viewerPath = buildAdapPdfViewerUrl(viewerTicket);
				result.setFilePath(viewerPath);
				result.setFileNm(tempFileInfo.getFileOrgNm());
				result.setSuccess(true);
				log.info("[VIEWER] file-api viewer prepared");
			} else if(param.getSessionUser().getAuthSite().equals("E")) {
				//외부서버에 옮겨질 파일 경로
				String outServerFilepath = SystemConfig.getSystemConfigValue("VIEWER_UPDOWN_PATH");
				String orgPathOutTemp = outServerFilepath.replace("\\\\", "\\");
				//http://211.197.235.163:9001/DaVuForEG/DaViewSvc?ediauto=T&filename=$D:\\DOCS\\FILE\\general\\ff\\f2\\UA20030925_1_D_0_01.PLT

				//외부서버로 파일 Copy 요청
				JSONObject fileCall = FileUtil.callSender(
						Seed128Cipher.encrypt(sViewerServerIp, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(oViewerServerIp, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(orgPath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(outServerFilepath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(tempFileInfo.getFileOrgNm(), Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING));
				String transferredFileName = FileUtil.requireSuccessfulTransferFileName(fileCall);
				log.info("[VIEWER] legacy transfer prepared");

				//외부서버 뷰어 호출 URL
				//String oViewerPath = null;
				String oViewerPath = orgPathOutTemp + transferredFileName;

				// 외부 서버로 파일을 옮긴 다음 파일 분할
				if(ext.equalsIgnoreCase("SVG")) {
					List<String> svgList = ViewerUtil.executeSvgFileParser(orgPathOutTemp + transferredFileName);

					if(svgList.size() > 0) {
						StringBuilder sb = new StringBuilder();

						for(String v: svgList) {
							if(!"".equals(sb.toString())) {
								sb.append("|");
								File f = new File(v);
								sb.append(f.getName());
							} else {
								sb.append(v);
							}
						}

						oViewerPath = sb.toString();
					}
				}

				result.setFilePath(SystemConfig.getSystemConfigValue("VIEWER_URL_OUT") + oViewerPath);
				result.setFileNm(tempFileInfo.getFileOrgNm());
				result.setSuccess(true);
				log.info("[VIEWER] external viewer prepared");
			} else {
				//내부서버 뷰어 호출 URL
				String sViewerPath = SystemConfig.getSystemConfigValue("VIEWER_URL") + orgPath;
				//String sViewerPath = null;

				// 파일 분할
				if(ext.equalsIgnoreCase("SVG")) {
					List<String> svgList = ViewerUtil.executeSvgFileParser(orgPath);

					if(svgList.size() > 0) {
						StringBuilder sb = new StringBuilder();

						for(String v: svgList) {
							if(!"".equals(sb.toString())) {
								sb.append("|");
								File f = new File(v);
								sb.append(f.getName());
							} else {
								sb.append(v);
							}

						}

						sViewerPath = SystemConfig.getSystemConfigValue("VIEWER_URL") + sb.toString();
					}
				}

				result.setFilePath(sViewerPath);
				result.setFileNm(tempFileInfo.getFileOrgNm());
				result.setSuccess(true);
				log.info("[VIEWER] internal viewer prepared");
			}

//			}else {
//				result.setSuccess(false);
//			}
			if(result.isSuccess()) {
				if(param.getSessionUser().getAuthSite().equals("E")) {
					CommonViewerParam watermarkParam = new CommonViewerParam();
					CommonViewerVO viewFileInfo = new CommonViewerVO();
					viewFileInfo = dao.selectFileInfo(param);
					if("Y".equals(tempFileInfo.getProtectYn())) {
						// 외부사용자 중  경우 워터마크만
						watermarkParam.setUserType("OUT");
						watermarkParam.setWatermarkType("GENERALPROTECT");
						watermarkParam.setRequestNo(param.getRequestNo());
						watermarkParam.setFileNo(param.getFileNo());
						watermarkParam.setObjectId(param.getObjectId());
					}else {
						// 외부사용자 중 방산기술일 경우 방산기술 + 워터마크
						watermarkParam.setUserType("OUT");
						watermarkParam.setWatermarkType("GENERAL");
						watermarkParam.setRequestNo(param.getRequestNo());
						watermarkParam.setFileNo(param.getFileNo());
						watermarkParam.setObjectId(param.getObjectId());
					}
					result.setWatermarkInfo(getWatermarkInfo(watermarkParam, viewFileInfo));
				}else {
					CommonViewerParam watermarkParam = new CommonViewerParam();

					if("Y".equals(tempFileInfo.getProtectYn())) {        //방산기술일 경우 뷰어에도 워터마크 표기
						watermarkParam.setWatermarkType("PROTECT");
					} else {
						watermarkParam.setWatermarkType("ITEM");
					}
					watermarkParam.setUserType("IN");
					result.setWatermarkInfo(getWatermarkInfo(watermarkParam, tempFileInfo));
					//}
				}
			}


			log.debug("[VIEWER] source resolved");
		}

		log.debug("[VIEWER] response prepared");

		return result;
	}


	public boolean copyFile(String orgPath, String targetPath) {
		boolean ret = true;
        try {
            BufferedInputStream source = new BufferedInputStream(new FileInputStream(new File(orgPath)));
            BufferedOutputStream destination = new BufferedOutputStream(new FileOutputStream(new File(targetPath)));
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            try {
                int n = 0;
                while (-1 != (n = source.read(buffer))) {
                    destination.write(buffer, 0, n);
                }
                destination.flush();
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (Exception e) {
			log.warn("[VIEWER_COPY] failed type={}", e.getClass().getSimpleName());
            ret = false;
        }
        return ret;
	}

	public CommonViewerVO getMergePrintInfo(CommonViewerParam param) throws ParseException {
		if (param == null) {
			throw new IllegalArgumentException("Merged print request is required.");
		}
		UserVO actor = securityAclService.requireCurrentUser();
		param.setSessionUser(actor);
		List<CommonViewerParam> printItems = buildMergePrintItems(param);
		if (printItems.isEmpty()) {
			throw new IllegalArgumentException("Merged print items are required.");
		}
		for (CommonViewerParam printItem : printItems) {
			printItem.setSessionUser(actor);
			requireFileAccess(printItem, SecurityAclService.PRINT);
		}
		/*
		 * A viewer ticket is bound to exactly one object/file ACL subject.
		 * Releasing a multi-resource result through the legacy ESOB/static URL
		 * would bypass per-item re-authorization when the result is fetched.
		 * Until an aggregate ticket schema and a server-side PDF merger exist,
		 * stop after validating every canonical item and release no job/path.
		 */
		CommonViewerVO secureRejection = rejectUnsafeMergePrint(printItems);
		if (secureRejection != null) {
			return secureRejection;
		}
		CommonViewerVO result = new CommonViewerVO();
		result.setSuccess(false);
		try {
			String strArrObjectId = "";
			String strRequestNo = "";
			String strFileNo = "";
			strArrObjectId = param.getObjectId();
			strRequestNo = param.getRequestNo();
			strFileNo = "0";

			//config table에서, 대상파일 경로 조희
			List<Map<String,Object>> dbConfig = pdao.selectDbConfig();
			String adapDocFilePath="";
			String adapRepoPath="";
			String adapOrgPath="";
			String adapPdfPath="";
			String adapPdfUrl = "";
			String mergePdfPath = "";
			String orgFileNm="";
			String cvrtFileNm="";
			String chkcvrtFilePathNm="";
			String orgFilePathNm="";
			String cvrtFilePathNm="";
			String cvrtFileUrl = "";
			String dirUrl = "";


			String strFeedObjectId = "";
			String strParamWmType = "";
			String strFeedObjectIdFilePathNm = "";

			String strObjectIdSql = "";

			String objectID = "";
			objectID = "";

			for(Map<String,Object> config : dbConfig) {
				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_ORG_FILE_PATH")) {
					adapDocFilePath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_REPO_PATH")) {
					adapRepoPath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_ORG_FILE_PATH")) {
					adapOrgPath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_PDF_PATH")) {
					adapPdfPath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
				if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_PDF_URL")) {
					adapPdfUrl = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
				if(config.get("SYSTEM_CONFIG_CD").equals("MERGE_PATH")) {
					mergePdfPath = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
			}

			//objctId별로 파일이 있는지 체크한후 없으면 생성

			String strObject = "";
			String strObjectType = "";
			String strWmType = "";
			String strWmTypeAdd = "";
			String TOTAL_PAGE_NO = "";
			String strTotalPageNo = "";
			String wmTypeCase = "";
			String paramWmType = "";
			String feedObjectId = "";
			String feedObjectIdFilePathNm = "";
			String strMergeFilePathNm = "";
			String strCopyFilePathNm = "";
			String strGuid = "";
			String strMergeFileUrl = "";
			String strMergedirUrl = "";

			int intCvrt = 10;
			int intCountCvrt = 0;


			strObjectIdSql = "";

			for(CommonViewerParam parsedItem : printItems) {
				if (parsedItem != null) {
					strObject = parsedItem.getObjectId();
					strObjectType = parsedItem.getObjectType();
					strWmType = parsedItem.getWatermarkType();

					// 출력 성공하면 개수 올려주는 테스트 코드
					objectID = strObject;
//					dao.updatePrintCnt(param);

					Map<String, Object> map = new HashMap<String, Object>();
					map.put("OBJECT_ID",strObject);
					map.put("FILE_NO", parsedItem.getFileNo());

					if (strObjectType.equals("문서") || strObjectType.equals("DOC")) {
						orgFileNm = pdao.selectFilePathNmDoc(map);
						strTotalPageNo = pdao.selectTotalPageNoDoc(map);
					} else if (strObjectType.equals("도면") || strObjectType.equals("DRAWING") ) {
						orgFileNm = pdao.selectFilePathNmDrawing(map);
						strTotalPageNo = pdao.selectTotalPageNoDrawing(map);
					}

//					if(orgFileNm == null || orgFileNm.equals("")){
//						// 파일을 찾지 못했을 경우.
//						result.setSuccess(false);
//						return result;
//					}

					// 윈도우 전용 가공 처리 23.05.15 koo
					orgFileNm = StoragePathUtils.toPath(orgFileNm).toString();

					orgFilePathNm = orgFileNm;
					cvrtFilePathNm = StoragePathUtils.resolve(
							adapPdfPath.replace("$", ""), objectID + ".pdf").toString();
					//chkcvrtFilePathNm = adapPdfPath+ "/" + objectID+".esob";
					chkcvrtFilePathNm = StoragePathUtils.resolve(
							adapPdfPath.replace("$", ""), objectID + ".esob").toString();
					cvrtFileUrl =  adapPdfUrl + "?file=" + "/out/destfile" + orgFileNm + ".esob";
					// dirUrl = adapPdfUrl + "?file=" + "/out/destfile/" + URLEncoder.encode(orgFileNm) + ".esob";
					intCvrt = 0;

					// 이미 파일 생성된 경우 변환 안 함 + 전체 페이지 수를 가져온다. 아니면 페이지 수 = 리턴 값
					File fileExist = new File(chkcvrtFilePathNm);
					if (fileExist.isFile()) {
						log.debug("[MERGE_PRINT] converted file cache hit");

						intCvrt = 1;
					} else {
						log.debug("[MERGE_PRINT] conversion requested");

						intCvrt = requestPathConvertApi(orgFilePathNm, adapPdfPath, objectID);
						// 변환 API 성공 응답이어도 실제 .esob 생성 여부로 최종 판단
						if (intCvrt > 0 && !(new File(chkcvrtFilePathNm).isFile())) {
							log.warn("[MERGE_PRINT] conversion output missing");
							intCvrt = 0;
						}
					}
					//이미 파일 생성된 경우 변환 안 함*/
					strTotalPageNo = String.valueOf(intCvrt);

					log.debug("[MERGE_PRINT] conversion status={}", intCvrt);

					//변환 결과를 CVRT에 입력

					//워터마크 정보를 생성
					//페이지수, 워터마크 타입 설정
					//wmType = 5@P|3@G|4@C  ( 1~5페이지는 P, 6~8페이지는 G, 9~12페이지는 C 타입)

					if (strWmType.equals("PROTECT")) {
						wmTypeCase = "P";
					} else if (strWmType.equals("CLASSIFIED")) {
						wmTypeCase = "C";
					} else if (strWmType.equals("GENERAL")) {
						wmTypeCase = "G";
					} else {
						wmTypeCase = "G";
					}

					//정상적인 경우는 워터마크 문자열 생성
					if (intCvrt > 0) {
						strWmTypeAdd = "|" + strTotalPageNo + "@" + wmTypeCase;
						paramWmType = paramWmType + strWmTypeAdd;
						feedObjectId = feedObjectId + "|" +  objectID;
						feedObjectIdFilePathNm = feedObjectIdFilePathNm + "|" + chkcvrtFilePathNm;
					}

				}
				intCountCvrt = intCountCvrt + 1;
				strObjectIdSql = strObjectIdSql + ",\'"+ objectID + "\'";
			}


			strObjectIdSql = strObjectIdSql.substring(1,strObjectIdSql.length() );


			strFeedObjectId= feedObjectId.substring(1, feedObjectId.length());
			strParamWmType= paramWmType.substring(1, paramWmType.length());
			strFeedObjectIdFilePathNm= feedObjectIdFilePathNm.substring(1, feedObjectIdFilePathNm.length());

			//머지 파일 생성 후 경로가져와서 DB입력

			log.debug("[MERGE_PRINT] convertedCount={}", intCountCvrt);

			strMergeFilePathNm = "";

			if( null == strFeedObjectIdFilePathNm ) {            //머지 대상이 없을경우
				result.setSuccess(false);
			} else {                                //머지 대상이 있는경우


				//오늘 날짜로 폴더 생성
				Calendar cal = Calendar.getInstance();
				String dateString;
				dateString = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
				File f = new File(mergePdfPath, dateString);
				if (!f.isDirectory() && !f.mkdirs()) {
					throw new IllegalStateException("병합 파일 디렉터리를 생성할 수 없습니다.");
				}

				// GUID 구함
				strGuid = pdao.selectGuid("");

				strMergeFilePathNm = new File(f, strGuid + ".pdf").getPath();
				strCopyFilePathNm = new File(f, strGuid + ".esob").getPath();
				//strMergeFilePathNm = mergePdfPath + "\\" +dateString + "\\" + "test.pdf";
				//strMergedirUrl = strMergedirUrl + "&ParamWmType=" + strParamWmType;


				// 파일이 1개면 이동
				if (intCountCvrt == 1 ) {
					copyFile(strFeedObjectIdFilePathNm, strCopyFilePathNm);
				}
				// 파일이 1개보다 크면 머지
				else if (intCountCvrt > 1) {
					throw new UnsupportedOperationException("네이티브 PDF 병합 기능은 제거되었습니다.");
				}

				log.debug("[MERGE_PRINT] merge status={}", intCvrt);

				// Defense in depth: even if the entry guard is changed later,
				// this legacy branch can never release a raw cache path.
				return rejectUnsafeMergePrint(printItems);
			}
		} catch(Exception e) {
			log.error("[MERGE_PRINT] failed type={}", e.getClass().getSimpleName());
			result.setSuccess(false);
			result.setPrintJobId(null);
			result.setFilePath(null);
			result.setWatermarkInfo(null);
			result.setFailType(MERGE_PRINT_SECURITY_REASON);
			result.setFailReason(MERGE_PRINT_SECURITY_MESSAGE);
		}

		return result;
	}

	private int requestPathConvertApi(String inputFilePath, String outputDir, String objectId) {
		HttpURLConnection connection = null;
		DataOutputStream requestStream = null;
		try {
			String endpoint = SystemConfig.getSystemConfigValue("CONVERT_SERVER_URL");
			if (endpoint == null || endpoint.trim().isEmpty()) {
				endpoint = "http://localhost:9001/api/internal/convert";
			}

			File inputFile = new File(inputFilePath);
			if (!inputFile.isFile()) {
				log.warn("[CONVERT_API] input file missing");
				return 0;
			}

			String boundary = "----FDMS-" + System.currentTimeMillis();
			URL url = new URL(endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			requestStream = new DataOutputStream(connection.getOutputStream());

			// outputDir 파트
			writeFormField(requestStream, boundary, "outputDir", outputDir);

			// files 파트
			String fileName = inputFile.getName();
			int dot = fileName.lastIndexOf('.');
			String ext = dot > -1 ? fileName.substring(dot) : "";
			String uploadName = objectId + ext;
			writeFilePart(requestStream, boundary, "files", uploadName, inputFile);

			requestStream.writeBytes("--" + boundary + "--\r\n");
			requestStream.flush();

			int status = connection.getResponseCode();
			String body = readHttpBody(connection, status >= 200 && status < 300);
			log.info("[CONVERT_API] completed status={}", status);
			if (status >= 200 && status < 300) {
				promoteConvertedPdfAsEsob(outputDir, objectId);
				return 1;
			}
		} catch (Exception e) {
			log.warn("[CONVERT_API] failed type={}", e.getClass().getSimpleName());
		} finally {
			try {
				if (requestStream != null) {
					requestStream.close();
				}
			} catch (Exception ignored) {}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return 0;
	}

	private void promoteConvertedPdfAsEsob(String outputDir, String objectId) {
		try {
			File targetEsob = StoragePathUtils.resolve(
					outputDir.replace("$", ""), objectId + ".esob").toFile();
			if (targetEsob.isFile()) {
				return;
			}

			File outDir = new File(outputDir);
			File[] candidates = outDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String lowerName = name.toLowerCase(Locale.ROOT);
					return lowerName.startsWith(objectId.toLowerCase(Locale.ROOT) + "_") && lowerName.endsWith(".pdf");
				}
			});

			if (candidates == null || candidates.length == 0) {
				return;
			}

			Arrays.sort(candidates, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return Long.compare(o2.lastModified(), o1.lastModified());
				}
			});

			copyFile(candidates[0].getAbsolutePath(), targetEsob.getAbsolutePath());
			log.info("[CONVERT_API] output promoted");
		} catch (Exception e) {
			log.warn("[CONVERT_API] output promotion failed type={}", e.getClass().getSimpleName());
		}
	}

	private void writeFormField(DataOutputStream out, String boundary, String name, String value) throws IOException {
		out.writeBytes("--" + boundary + "\r\n");
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
		out.write(value.getBytes(StandardCharsets.UTF_8));
		out.writeBytes("\r\n");
	}

	private void writeFilePart(DataOutputStream out, String boundary, String partName, String fileName, File file) throws IOException {
		out.writeBytes("--" + boundary + "\r\n");
		out.writeBytes("Content-Disposition: form-data; name=\"" + partName + "\"; filename=\"" + fileName + "\"\r\n");
		out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		out.writeBytes("\r\n");
	}

	private String readHttpBody(HttpURLConnection conn, boolean success) {
		InputStream is = null;
		try {
			is = success ? conn.getInputStream() : conn.getErrorStream();
			if (is == null) {
				return "";
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int read;
			while ((read = is.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}
			return bos.toString("UTF-8");
		} catch (Exception ignored) {
			return "";
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception ignored) {}
		}
	}


	public CommonViewerVO getPrintInfo(CommonViewerParam param) {
		bindActorAndRequire(param, SecurityAclService.PRINT);
		CommonViewerVO result = new CommonViewerVO();
		result.setSuccess(false);
		result.setPrintJobId(null);
		result.setFilePath(null);
		result.setWatermarkInfo(null);
		result.setFailType(PRINT_CALLBACK_REQUIRED_REASON);
		result.setFailReason(PRINT_CALLBACK_REQUIRED_MESSAGE);
		log.warn("[PRINT_VIEWER][DENY] reason={}", PRINT_CALLBACK_REQUIRED_REASON);
		return result;
	}

	@SuppressWarnings("unused")
	private CommonViewerVO prepareLegacyPrintInfo(CommonViewerParam param) throws ParseException {
		bindActorAndRequire(param, SecurityAclService.PRINT);
		CommonViewerVO result = new CommonViewerVO();

		try {
			String outServerFilepath = SystemConfig.getSystemConfigValue("UPDOWN_PRINT_PATH");

			String filePath = "";
			String filePathOut = "";
			CommonViewerVO fileInfo = new CommonViewerVO();

			fileInfo = getFileInfo(param);

			if( null == fileInfo ) {            //파일이 없을경우
				result.setSuccess(false);
			}
//			else if( fileInfo.getPrintCount() >= Integer.parseInt(SystemConfig.getSystemConfigValue("PRINT_COUNT")) ) {
//				result.setSuccess(false);
//				result.setFailType("OVER_PRINT_COUNT");
//				result.setFailReason(fileInfo.getFileOrgNm());
//			}
			else if("1".equals(fileInfo.getDestroyStatusCd()) || "2".equals(fileInfo.getDestroyStatusCd()) || "3".equals(fileInfo.getDestroyStatusCd())) {
				// 1: 폐기중, 2: 폐기요청, 3: 폐기승인
				// 폐기상태일 경우 출력 불가
				result.setSuccess(false);
				result.setFailType("DESTROY");
				result.setFailReason("폐기상태인 자료는 출력할 수 없습니다.");
			} else {                                //파일이 있는경우
				String ext = fileInfo.getFileOrgNm().substring(fileInfo.getFileOrgNm().lastIndexOf(".")+1, fileInfo.getFileOrgNm().length());
				if (!"PDF".equalsIgnoreCase(ext)) {
					throw new UnsupportedOperationException(
							"Printing is limited to ticket-backed PDF files.");
				}
//			for(CommonViewerVO tempFileInfo : fileInfo) {
//				String ext = fileInfo.getFileOrgNm().substring(fileInfo.getFileOrgNm().lastIndexOf(".")+1, fileInfo.getFileOrgNm().length());
//				String tempPath = SystemConfig.getSystemConfigValue("VIEWER_TEMP_PATH").replace("$", "");
//				String copyPath = tempPath + fileInfo.getFileOrgNm();
//				String tempFileNm = fileInfo.getFileNm() + "." + ext;
//				String copyPath = tempPath + tempFileNm;

//				File temp = new File(tempPath);
//				if(temp.isDirectory()) {
//					temp.mkdir();
//				}
//				String orgPath = "";
				if( ("UNREG".equals(param.getRequestType())) || ("UNREG_DISTRIBUTION".equals(param.getRequestType())) ) {
					filePath = fileInfo.getFilePath();
				}else {
					filePath = StoragePathUtils.resolve(
							SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH"),
							fileInfo.getFilePath()).toString();
					filePathOut = outServerFilepath + fileInfo.getFileOrgNm();
//						orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + tempFileInfo.getFileNm();
				}

				//내부서버 네트워크 드라이브 원본 경로
				String orgPath = StoragePathUtils.resolve(
						SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH"),
						fileInfo.getFilePath()).toString();
				String sViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE");
				String oViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE");
				String fileTransId = "";
				if(param.getSessionUser().getAuthSite().equals("E") && !"PDF".equalsIgnoreCase(ext)) {
					//외부서버 출력
					result.setFileOrgNm(fileInfo.getFileOrgNm());
					log.debug("[PRINT_VIEWER] external source prepared");
					//http://211.197.235.163:9001/DaVuForEG/DaViewSvc?ediauto=T&filename=$D:\\DOCS\\FILE\\general\\ff\\f2\\UA20030925_1_D_0_01.PLT

					//외부서버로 파일 Copy 요청
					JSONObject fileCall = FileUtil.callSender(
							Seed128Cipher.encrypt(sViewerServerIp, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
							Seed128Cipher.encrypt(oViewerServerIp, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
							Seed128Cipher.encrypt(orgPath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
							Seed128Cipher.encrypt(outServerFilepath, Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING),
							Seed128Cipher.encrypt(fileInfo.getFileOrgNm(), Constant.legacyCryptoKeyBytes(), Constant.SEED_ENCODING));
					fileTransId = requireSuccessfulTransfer(fileCall);

//					String oViewerPath = orgPathOutTemp + fileCall.get("fileNm");
//
//					// 외부 서버로 파일을 옮긴 다음 파일 분할
//					if(ext.equalsIgnoreCase("SVG")) {
//						List<String> svgList = ViewerUtil.executeSvgFileParser(orgPathOutTemp + fileCall.get("fileNm"));
//
//						if(svgList.size() > 0) {
//							StringBuilder sb = new StringBuilder();
//
//							for(String v: svgList) {
//								if(!"".equals(sb.toString())) {
//									sb.append("|");
//									File f = new File(v);
//									sb.append(f.getName());
//								}
//								else {
//									sb.append(v);
//								}
//							}
//
//							oViewerPath = sb.toString();
//						}
//					}
//
//					result.setFilePath(SystemConfig.getSystemConfigValue("VIEWER_URL_OUT") + oViewerPath);

					log.info("[PRINT_VIEWER] legacy transfer prepared");
				}else {
					//내부서버 출력
					result.setFileOrgNm(fileInfo.getFileOrgNm());
					log.debug("[PRINT_VIEWER] internal source prepared");
				}

//				log.info("copyPath : " + copyPath);
//				if(copyFile(orgPath, copyPath)) {
//					log.info("copy 성공");
//					String rtnFilePath = "";
//					if("".equals(filePath)) {
//						if( ("".equals(SystemConfig.getSystemConfigValue("VIEWER_PATH"))) || (null == SystemConfig.getSystemConfigValue("VIEWER_PATH") ) ) {
//							rtnFilePath = SystemConfig.getSystemConfigValue("VIEWER_TEMP_PATH") + tempFileNm;
//						}else {
//							rtnFilePath = SystemConfig.getSystemConfigValue("VIEWER_PATH") + tempFileNm;
//						}
//						filePath = rtnFilePath;
//					}else {
//						filePath += '|' + fileInfo.getFileOrgNm();
//					}

//					filePath = orgPath;

//				}else {
//					result.setSuccess(false);
//				}
//			}

				if(!isBlankValue(result.getFileOrgNm())) {
					//워터마크
					filePath = StoragePathUtils.toPath(filePath).toString();
//					filePath = filePath.replaceAll("a2085qdfpkgoaqow3mamhtmyn8hupqsbiqf8d1t8p6c.g4w", "111.txt");
					log.debug("[PRINT_VIEWER] source normalized");

					String viewerCallUrl = "";

					if(param.getSessionUser().getAuthSite().equals("E") && !"PDF".equalsIgnoreCase(ext)) {
						//viewerCallUrl = SystemConfig.getSystemConfigValue("VIEWER_URL_OUT") + outServerFilepath + fileTransId;

						viewerCallUrl = outServerFilepath + fileTransId;

						// 외부 서버로 파일을 옮긴 다음 파일 분할
						if(ext.equalsIgnoreCase("SVG")) {
							List<String> svgList = ViewerUtil.executeSvgFileParser(viewerCallUrl);

							if(svgList.size() > 0) {
								StringBuilder sb = new StringBuilder();

								for(String v: svgList) {
									if(!"".equals(sb.toString())) {
										sb.append("|");
										File f = new File(v);
										sb.append(f.getName());
									} else {
										sb.append(v);
									}
								}

								viewerCallUrl = sb.toString();
							}
						}

						result.setFilePath(SystemConfig.getSystemConfigValue("VIEWER_URL_OUT") + viewerCallUrl);
					}else {
						viewerCallUrl = SystemConfig.getSystemConfigValue("VIEWER_URL") + filePath;

						//내부서버 뷰어 호출 URL
//						String sViewerPath = SystemConfig.getSystemConfigValue("VIEWER_URL") + orgPath;
						//String sViewerPath = null;

						// 파일 분할
						if(ext.equalsIgnoreCase("SVG")) {
							List<String> svgList = ViewerUtil.executeSvgFileParser(orgPath);

							if(svgList.size() > 0) {
								StringBuilder sb = new StringBuilder();

								for(String v: svgList) {
									if(!"".equals(sb.toString())) {
										sb.append("|");
										File f = new File(v);
										sb.append(f.getName());
									} else {
										sb.append(v);
									}

								}

								viewerCallUrl = SystemConfig.getSystemConfigValue("VIEWER_URL") + sb.toString();
							}
						}

						result.setFilePath(viewerCallUrl);
					}

					String cachedPdfName = cacheLocalPdfForViewer(filePath);
					String viewerTicket = viewerTicketService.issue(param, cachedPdfName);
					result.setFilePath(buildAdapPdfViewerUrl(viewerTicket));
					log.info("[PRINT_VIEWER] ticket-backed viewer prepared");
					CommonViewerParam watermarkParam = new CommonViewerParam();
					watermarkParam.setUserType(param.getUserType());
					watermarkParam.setWatermarkType(param.getWatermarkType());
					result.setWatermarkInfo(getWatermarkInfo(watermarkParam, fileInfo));

					log.debug("[PRINT_VIEWER] response prepared");

					// 실제 성공 콜백 전에는 출력 횟수를 올리지 않는다.
					if (isBlankValue(result.getFilePath())) {
						throw new IllegalStateException("Print viewer path was not prepared.");
					}
					result.setPrintJobId(printAuditService.start(param));
					result.setSuccess(true);
				}else {

				}
			}
		} catch (Constant.LegacyCryptoConfigurationException e) {
			log.error("Legacy print encryption is unavailable; configure KT1B_LEGACY_CRYPTO_KEY");
			throw e;
		} catch(Exception e) {
			log.error("[PRINT_VIEWER] failed type={}", e.getClass().getSimpleName());
			result.setSuccess(false);
			result.setPrintJobId(null);
			result.setFilePath(null);
			result.setFileOrgNm(null);
			result.setFailType("PRINT_PREPARE_FAILED");
			result.setFailReason("Print file preparation failed.");
		}

		return result;
	}

	static String requireSuccessfulTransfer(JSONObject transfer) {
		return FileUtil.requireSuccessfulTransferFileName(transfer);
	}

	private boolean isBlankValue(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String getWatermarkInfo(CommonViewerParam param, CommonViewerVO fileInfo) throws ParseException {
		String watermarkInfo = "";
		if( (!"".equals(param.getWatermarkType())) && (!(null==param.getWatermarkType())) ) {
			List<WatermarkVO> listWm = dao.selectWatermarkInfo(param);
			for(WatermarkVO tempVo : listWm) {
				if(!"".equals(watermarkInfo)) {
					watermarkInfo += "#";
				}
				if("MSG".equals(tempVo.getItemType()) ) {
					watermarkInfo += tempVo.getItemInfo() + "|" + tempVo.getItemValign() + "|" + tempVo.getItemHAlign() + "|" +
							tempVo.getItemFont() + "|" + tempVo.getItemColor() + "|" + tempVo.getItemFontStyle() + "|" + tempVo.getItemTransparency();
				} else if("IMG".equals(tempVo.getItemType())) {
					if("".equals(watermarkInfo)) {
						watermarkInfo = tempVo.getItemInfo() + "|" + tempVo.getItemValign() + "|" + tempVo.getItemHAlign() + "|" +
								tempVo.getItemFont() + "|" + tempVo.getItemColor() + "|" + tempVo.getItemFontStyle() + "|" + tempVo.getItemTransparency();
					} else {
						String temp = watermarkInfo;
						watermarkInfo = tempVo.getItemInfo() + "|CENTER|CENTER|" +
								tempVo.getItemFont() + "|" + tempVo.getItemColor() + "|" + tempVo.getItemFontStyle() + "|" + tempVo.getItemTransparency();
						watermarkInfo += "#" + temp;
					}
				} else if("OBJECT".equals(tempVo.getItemType())) {
					String arrObject[] = tempVo.getItemInfo().split("[|]");
					for(String strTemp : arrObject) {
						CommonViewerParam langParam = new CommonViewerParam();
						String strItemText = "";
						String strItemValue = "";
						langParam.setMsgCode(strTemp);
						strItemText += dao.getMessageDesc(langParam);
						if("userNm".equals(strTemp)) {                                            // 출력자
							strItemValue = param.getSessionUser().getUserNm();
						}else if("deployDt".equals(strTemp)) {                                    // 배포일자
							String strDate = fileInfo.getDeployDt();
							if(strDate != null) {
								SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
								Date to = transFormat.parse(strDate);
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
								strItemValue = format.format(to);
							}
						}else if("endDate".equals(strTemp)) {                                    // 유효기간
							String strDate = fileInfo.getEndDate();
							if(strDate != null) {
								SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd");
								Date to = transFormat.parse(strDate);
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
								strItemValue = format.format(to);
							}
						}else if("printDate".equals(strTemp)) {                                    // 출력일자
							Date date = new Date();
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
							strItemValue = format.format(date);
						}else if("businessTypeNm".equals(strTemp)) {                            // 사업유형
							strItemValue = fileInfo.getBusinessTypeNm();
						}else if("distributeTypeNm".equals(strTemp)) {                            // 배포유형
							strItemValue = fileInfo.getDistributeTypeNm();
						}else if("purchaserAppDate".equals(strTemp)) {                            // 구매담당자 승인일
							if(param.getSessionUser().getAuthSite().equals("E")) {
								strItemValue = fileInfo.getPurchaserAppDate();
							}
						}else if("requestUserNm".equals(strTemp)) {                                // 요청자
							strItemValue = fileInfo.getRequestUserNm();
						}else if("requestPurpose".equals(strTemp)) {
							strItemValue = fileInfo.getRequestPurpose();
						}
						if( !("".equals(strItemValue)) && !(null==strItemValue)) {
							watermarkInfo += strItemText + strItemValue + ", ";
						}
					}
					if( !("".equals(watermarkInfo)) ) {
						watermarkInfo = watermarkInfo.substring(0, watermarkInfo.length()-2);
					}

					watermarkInfo += "|" + tempVo.getItemValign() + "|" + tempVo.getItemHAlign() + "|" +
							tempVo.getItemFont() + "|" + tempVo.getItemColor() + "|" + tempVo.getItemFontStyle() + "|" + tempVo.getItemTransparency();
				}
			}

		}else {

		}

		return watermarkInfo;
	}


	public void insertRequest(RequestParam param) {
		commonRequestDao.insertDocsRequest(param);
		commonRequestDao.insertDocsRequestMapping(param);
		commonRequestDao.insertDocsRequestDetail(param);
		commonRequestDao.insertDocsRequestDeploy(param);
		if("DISTRIBUTION".equals(param.getRequestType())) {
			commonRequestDao.insertDocsRequestFile(param);
		}
	}


	public void updatePrintCnt(CommonViewerParam param){
		throw new UnsupportedOperationException("출력 횟수는 print-result 성공 콜백에서만 갱신됩니다.");
	}

	private CommonViewerVO rejectUnsafeMergePrint(List<CommonViewerParam> printItems) {
		CommonViewerVO result = new CommonViewerVO();
		result.setSuccess(false);
		result.setPrintJobId(null);
		result.setFilePath(null);
		result.setWatermarkInfo(null);
		result.setFailType(MERGE_PRINT_SECURITY_REASON);
		result.setFailReason(MERGE_PRINT_SECURITY_MESSAGE);
		log.warn("[MERGE_PRINT][DENY] reason={}, itemCount={}",
				MERGE_PRINT_SECURITY_REASON, printItems == null ? 0 : printItems.size());
		return result;
	}

	private void bindActorAndRequire(CommonViewerParam param, String actionCd) {
		if (param == null) {
			throw new IllegalArgumentException("자료 요청값이 없습니다.");
		}
		UserVO actor = securityAclService.requireCurrentUser();
		param.setSessionUser(actor);
		requireFileAccess(param, actionCd);
	}

	private void requireFileAccess(CommonViewerParam param, String actionCd) {
		FileAccessRequest request = new FileAccessRequest();
		request.setActionCd(actionCd);
		request.setObjectType(toAclObjectType(param.getObjectType(), param.getRequestType()));
		request.setObjectId(param.getObjectId());
		request.setFileNo(param.getFileNo());
		request.setRequestNo(param.getRequestNo());
		securityAclService.requireAccess(request);
	}

	private List<CommonViewerParam> buildMergePrintItems(CommonViewerParam param) {
		List<CommonViewerParam> legacyItems = new ArrayList<CommonViewerParam>();
		if (param.getObjectId() == null) return legacyItems;
		for (String encoded : param.getObjectId().split("__")) {
			if (encoded != null && !encoded.trim().isEmpty()) {
				legacyItems.add(parseLegacyMergeItem(encoded, param));
			}
		}
		if (param.getList() == null || param.getList().isEmpty()) {
			for (CommonViewerParam item : legacyItems) {
				if (isBlankValue(item.getFileNo())) {
					throw new IllegalArgumentException("Merged print file number is required.");
				}
			}
			return legacyItems;
		}
		if (legacyItems.size() != param.getList().size()) {
			throw new IllegalArgumentException("Merged print item representations do not match.");
		}

		List<CommonViewerParam> canonicalItems = new ArrayList<CommonViewerParam>();
		for (int index = 0; index < legacyItems.size(); index++) {
			CommonViewerParam encodedItem = legacyItems.get(index);
			CommonViewerParam listedItem = param.getList().get(index);
			if (listedItem == null
					|| !sameValue(encodedItem.getObjectId(), listedItem.getObjectId())
					|| !sameValue(encodedItem.getObjectType(), listedItem.getObjectType())
					|| !sameValue(encodedItem.getWatermarkType(), listedItem.getWatermarkType())
					|| isBlankValue(listedItem.getFileNo())) {
				throw new IllegalArgumentException("Merged print item representations do not match.");
			}
			CommonViewerParam canonical = new CommonViewerParam();
			canonical.setObjectId(encodedItem.getObjectId());
			canonical.setObjectType(encodedItem.getObjectType());
			canonical.setWatermarkType(encodedItem.getWatermarkType());
			canonical.setRequestNo(listedItem.getRequestNo());
			canonical.setRequestType(listedItem.getRequestType());
			canonical.setFileNo(listedItem.getFileNo());
			canonical.setUserType(listedItem.getUserType());
			canonicalItems.add(canonical);
		}
		return canonicalItems;
	}

	private boolean sameValue(String left, String right) {
		return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
	}

	private CommonViewerParam parseLegacyMergeItem(String encoded, CommonViewerParam parent) {
		int last = encoded.lastIndexOf('_');
		int second = last < 0 ? -1 : encoded.lastIndexOf('_', last - 1);
		if (second <= 0 || last <= second) {
			throw new IllegalArgumentException("병합 출력 자료 식별자가 올바르지 않습니다.");
		}
		CommonViewerParam item = new CommonViewerParam();
		item.setObjectId(encoded.substring(0, second));
		item.setObjectType(encoded.substring(second + 1, last));
		item.setWatermarkType(encoded.substring(last + 1));
		item.setRequestNo(parent.getRequestNo());
		item.setRequestType(parent.getRequestType());
		item.setFileNo(parent.getFileNo());
		return item;
	}

	private String toAclObjectType(String objectType, String requestType) {
		if ("문서".equals(objectType)) return "DOCUMENT";
		if ("도면".equals(objectType)) return "DRAWING";
		if ("PRODUCT".equals(requestType) && "DOC".equalsIgnoreCase(objectType)) return "PRODUCT_DOCUMENT";
		if ("PRODUCT".equals(requestType) && "SW".equalsIgnoreCase(objectType)) return "PRODUCT_SW";
		return objectType;
	}

	private boolean isFileApiPath(String filePath) {
		String[] fileApiPath = splitFileApiPath(filePath);
		boolean result = fileApiPath != null;
		log.debug("[FILE_API_VIEWER] source matched={}", result);
		return result;
	}

	private String cacheFileApiFileForViewer(String filePath, String orgFileNm) {
		String[] fileApiPath = splitFileApiPath(filePath);
		if (fileApiPath == null) {
			throw new IllegalStateException("File API fileName is empty: " + filePath);
		}
		String folder = fileApiPath[0];
		String fileName = fileApiPath[1];

		byte[] bytes = fileApiClient.download(fileName, folder);
		String cacheDir = resolveViewerCacheDir();
		log.debug("[FILE_API_VIEWER] cache write bytes={}", bytes.length);
		File dir = new File(cacheDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("Viewer cache directory cannot be created: " + cacheDir);
		}
		File target = new File(dir, fileName);
		try (OutputStream output = new FileOutputStream(target)) {
			output.write(bytes);
		} catch (IOException e) {
			throw new IllegalStateException("Viewer cache write failed: " + target.getAbsolutePath(), e);
		}
		log.info("[FILE_API_VIEWER] cached success={}, size={}", target.isFile(), target.length());
		return target.getAbsolutePath();
	}

	private String cacheLocalPdfForViewer(String sourcePath) {
		File source = StoragePathUtils.toPath(sourcePath).toFile();
		if (!source.isFile() || !source.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
			throw new IllegalStateException("Ticket-backed PDF source is unavailable.");
		}
		File cacheDir = StoragePathUtils.toPath(resolveViewerCacheDir()).toFile();
		if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
			throw new IllegalStateException("Viewer cache directory cannot be created.");
		}
		String cachedName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
		File cached = new File(cacheDir, cachedName);
		try {
			Files.copy(source.toPath(), cached.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			throw new IllegalStateException("Viewer cache write failed.", exception);
		}
		return cachedName;
	}

	private String[] splitFileApiPath(String filePath) {
		String path = normalizeFileApiPath(filePath);
		if (path.isEmpty() || path.startsWith("/") || path.matches("^[A-Za-z]:/.*")) {
			return null;
		}
		int separator = path.indexOf("/");
		if (separator <= 0 || separator == path.length() - 1) {
			return null;
		}
		String folder = path.substring(0, separator);
		String fileName = path.substring(separator + 1);
		if (!fileName.toLowerCase().endsWith(".pdf")) {
			return null;
		}
		return new String[] { folder, fileName };
	}

	private String normalizeFileApiPath(String filePath) {
		return filePath == null ? "" : filePath.trim().replace("\\", "/");
	}

	private String resolveViewerCacheDir() {
		String cacheDir = SystemConfig.getSystemConfigValue("ADAP_PDF_PATH");
		if (cacheDir == null || cacheDir.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_PATH is empty");
		}
		return StoragePathUtils.toPath(cacheDir.replace("$", "").trim()).toString();
	}

	private String buildAdapPdfViewerUrl(String ticketKey) throws UnsupportedEncodingException {
		String adapPdfUrl = SystemConfig.getSystemConfigValue("ADAP_PDF_URL");
		if (adapPdfUrl == null || adapPdfUrl.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_URL is empty");
		}
		String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/common/viewer/pdf-cache/")
				.path(ticketKey)
				.build()
				.toUriString();
		return requireSecureViewerUrl(adapPdfUrl) + "?file=" + URLEncoder.encode(fileUrl, "UTF-8");
	}

	private String requireSecureViewerUrl(String rawUrl) {
		try {
			URI uri = URI.create(rawUrl == null ? "" : rawUrl.trim());
			String host = uri.getHost();
			boolean loopbackHttp = "http".equalsIgnoreCase(uri.getScheme())
					&& ("localhost".equalsIgnoreCase(host)
							|| "127.0.0.1".equals(host)
							|| "::1".equals(host));
			if (host == null || uri.getUserInfo() != null || uri.getFragment() != null
					|| (!"https".equalsIgnoreCase(uri.getScheme()) && !loopbackHttp)) {
				throw new IllegalArgumentException("Viewer endpoint must use HTTPS.");
			}
			return uri.toString();
		} catch (RuntimeException exception) {
			throw new IllegalStateException("Viewer endpoint is not securely configured.", exception);
		}
	}
}
