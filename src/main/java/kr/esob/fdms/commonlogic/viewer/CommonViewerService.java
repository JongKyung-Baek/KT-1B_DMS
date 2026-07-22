package kr.esob.fdms.commonlogic.viewer;


import epdf.epdfconvert;
import kr.esob.fdms.commonlogic.fileapi.FileApiClient;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.message.CommonMessageContainer;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.inside.unregisted.request.UnregisterRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

	private final FileApiClient fileApiClient = new FileApiClient();

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
	private static final String EXCEPTION_EXT[] = {"zip", "exe"};

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
	System.out.println("yskim_test: in");

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
		System.out.println("yskim_test: getDestroyStatus, destroy....");

		return true;
	} else {
		System.out.println("yskim_test: getDestroyStatus, not destroyed");

		return false;
	}
}

	public boolean getDestroyStatus_printHistory(CommonViewerParam param) throws ParseException, UnsupportedEncodingException {

		String tempFileInfo_printHistory = dao.getFileInfoForDestroyStatus_printHistory(param);
		// 프린트 히스토리의 VO에 데이터 담으면 됨. dao로 확인할때, 출력이력 보여주는 db확인 하면 됨.
		// 거기서 폐기요청자 들어 있으면 저 아래 식에 넣어두는거
		System.out.println("tempFileInfo_printHistory:  " + tempFileInfo_printHistory);
//		임시방편으로 처리해둠
		if (tempFileInfo_printHistory == null) {
			return false;
		}


		if (tempFileInfo_printHistory != null) {
			System.out.println("yskim_test: getDestroyStatus, destroy....");

			return true;
		} else {
			System.out.println("yskim_test: getDestroyStatus, not destroyed");

			return false;
		}
	}










	public CommonViewerVO getViewFilePath(CommonViewerParam param) throws ParseException, UnsupportedEncodingException {
		CommonViewerVO result = new CommonViewerVO();
		if(param.getSessionUser().getAuthSite().equals("E")) {
			result.setViewerCabUrl(SystemConfig.getSystemConfigValue("VIEWER_CAB_URL_OUT"));
		}else {
			result.setViewerCabUrl(SystemConfig.getSystemConfigValue("VIEWER_CAB_URL"));
		}
		//파일 정보 조회
		CommonViewerVO tempFileInfo = getFileInfo(param);
		result.setRequestType(param.getRequestType());
		System.out.println("yskim_test: tempFileInfo (" + tempFileInfo + ")");

		//파일 Copy
//		File orgFile = new File(SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + tempFileInfo.getFilePath());

		if( null==tempFileInfo ) {            //파일이 없을경우
			System.out.println("yskim_test: 파일이 없습니다. setSuccess(false)");
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
				System.out.println("[FILE_API_VIEWER] file api path detected. filePath=" + tempFileInfo.getFilePath());
				orgPath = cacheFileApiFileForViewer(tempFileInfo.getFilePath(), tempFileInfo.getFileOrgNm());
				orgPathOut = orgPath;
			} else if( ("UNREG".equals(param.getRequestType())) || ("UNREG_DISTRIBUTION".equals(param.getRequestType())) ) {
				orgPath = tempFileInfo.getFilePath().replaceAll("/", "\\\\");
				System.out.println("yskim_test: orgPath" + orgPath);
			}else {
				orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + tempFileInfo.getFilePath().replaceAll("/", "\\\\");
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
				String viewerPath = buildAdapPdfViewerUrl(new File(orgPath).getName());
				result.setFilePath(viewerPath);
				result.setFileNm(tempFileInfo.getFileOrgNm());
				result.setSuccess(true);
				log.info("FILE_API viewer URL : " + viewerPath);
			} else if(param.getSessionUser().getAuthSite().equals("E")) {
				//외부서버에 옮겨질 파일 경로
				String outServerFilepath = SystemConfig.getSystemConfigValue("VIEWER_UPDOWN_PATH");
				String orgPathOutTemp = outServerFilepath.replace("\\\\", "\\");
				//http://211.197.235.163:9001/DaVuForEG/DaViewSvc?ediauto=T&filename=$D:\\DOCS\\FILE\\general\\ff\\f2\\UA20030925_1_D_0_01.PLT

				//외부서버로 파일 Copy 요청
				JSONObject fileCall = FileUtil.callSender(
						Seed128Cipher.encrypt(sViewerServerIp, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(oViewerServerIp, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(orgPath, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(outServerFilepath, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)
						, Seed128Cipher.encrypt(tempFileInfo.getFileOrgNm(), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING));
				//System.out.println("fileNm ================================> " + fileCall.get("fileNm"));
				log.info("원본 서버 아이피 : "+sViewerServerIp);
				log.info("카피 서버 아이피 : "+oViewerServerIp);
				log.info("원본 경로 : "+orgPath);
				log.info(fileCall.toString());

				//외부서버 뷰어 호출 URL
				//String oViewerPath = null;
				String oViewerPath = orgPathOutTemp + fileCall.get("fileNm");
				//System.out.println("oViewerPath ================================> " + oViewerPath);

				// 외부 서버로 파일을 옮긴 다음 파일 분할
				if(ext.equalsIgnoreCase("SVG")) {
					List<String> svgList = ViewerUtil.executeSvgFileParser(orgPathOutTemp + fileCall.get("fileNm"));

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
				//System.out.println("외부서버 뷰어 호출 URL ================> " + oViewerPath);
				log.info(oViewerPath);
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
				//System.out.println("내부서버 뷰어 호출 URL ================> " + sViewerPath);
				log.info(sViewerPath);
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


			log.debug("orgPath : " + orgPath);
		}

		log.debug(JSONObject.fromObject(result).toString());

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
			e.printStackTrace();
			System.out.println(e.getMessage());
            ret = false;
        }
        return ret;
	}

	public CommonViewerVO getMergePrintInfo(CommonViewerParam param) throws ParseException {

		CommonViewerVO result = new CommonViewerVO();
		try {
			String strArrObjectId = "";
			String strRequestNo = "";
			String strFileNo = "";
			strArrObjectId = param.getObjectId();
			strRequestNo = param.getRequestNo();
			strFileNo = "0";
			System.out.println("strArrObjectId = " + strArrObjectId);
			System.out.println("strRequestNo = " + strRequestNo);

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
			String FilePath3D ="";


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
				if(config.get("SYSTEM_CONFIG_CD").equals("3D_FILE_PATH")){
					FilePath3D = config.get("SYSTEM_CONFIG_VALUE").toString();
					System.out.println("FilePath3D =>>>>>>>>>>> " + FilePath3D);
				}
			}

			//objctId별로 파일이 있는지 체크한후 없으면 생성

			String[] arraysStr = strArrObjectId.split("__");
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

			//System.out.println(ArraysStr);

			strObjectIdSql = "";

			for(String s : arraysStr) {
				if( s != null ) {
					String[] arraysStr2 = s.split("_");
					System.out.println(s);

					strObject = arraysStr2[0];
					strObjectType = arraysStr2[1];
					strWmType = arraysStr2[2];

					// 출력 성공하면 개수 올려주는 테스트 코드
					objectID = strObject;
					param.setObjectId(objectID);
//					dao.updatePrintCnt(param);

					Map<String, Object> map = new HashMap<String, Object>();
					map.put("OBJECT_ID",strObject);
					System.out.println("OBJECT_ID -> " + strObject);
					System.out.println("strObjectType -> " + strObjectType);

					if (strObjectType.equals("문서") || strObjectType.equals("DOC")) {
						System.out.println("objectType= DOC = "+strObjectType);
						orgFileNm = pdao.selectFilePathNmDoc(map);
						strTotalPageNo = pdao.selectTotalPageNoDoc(map);
					} else if (strObjectType.equals("도면") || strObjectType.equals("DRAWING") ) {
						System.out.println("objectType= Drawing = "+strObjectType);
						orgFileNm = pdao.selectFilePathNmDrawing(map);
						System.out.println("orgFileNm =>>>>>>>>> " + orgFileNm);
						if(orgFileNm.contains(FilePath3D)){
							result.setSuccess(false);
							result.setFailReason("3D ");
							result.setFailType("NO_SUPPORT_EXT");
							System.out.println("실패 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
							return result;
						}
						strTotalPageNo = pdao.selectTotalPageNoDrawing(map);
					}

//					if(orgFileNm == null || orgFileNm.equals("")){
//						// 파일을 찾지 못했을 경우.
//						result.setSuccess(false);
//						return result;
//					}

					// 윈도우 전용 가공 처리 23.05.15 koo
					orgFileNm = orgFileNm.replace("/", "\\");

					System.out.println("orgFileNm -> " + orgFileNm);
					orgFilePathNm = orgFileNm;
					cvrtFilePathNm = adapPdfPath + "\\" + objectID + ".pdf";
					//chkcvrtFilePathNm = adapPdfPath+ "/" + objectID+".esob";
					chkcvrtFilePathNm = adapPdfPath+ "\\" + objectID+".esob";
					System.out.println("chkcvrtFilePathNm -> " + chkcvrtFilePathNm);
					cvrtFileUrl =  adapPdfUrl + "?file=" + "/out/destfile" + orgFileNm + ".esob";
					// dirUrl = adapPdfUrl + "?file=" + "/out/destfile/" + URLEncoder.encode(orgFileNm) + ".esob";
					intCvrt = 0;

					// 이미 파일 생성된 경우 변환 안 함 + 전체 페이지 수를 가져온다. 아니면 페이지 수 = 리턴 값
					File fileExist = new File(chkcvrtFilePathNm);
					if (fileExist.isFile()) {
						System.out.println("cvrtFilePathNm exists : " + chkcvrtFilePathNm);

						intCvrt = 1;
					} else {
						System.out.println("getMergePrintInfo, if (fileExist.isFile()) else orgFilePathNm " + orgFilePathNm);
						System.out.println("getMergePrintInfo, if (fileExist.isFile()) else cvrtFilePathNm " + cvrtFilePathNm);

						intCvrt = requestPathConvertApi(orgFilePathNm, adapPdfPath, objectID);
						// 변환 API 성공 응답이어도 실제 .esob 생성 여부로 최종 판단
						if (intCvrt > 0 && !(new File(chkcvrtFilePathNm).isFile())) {
							System.out.println("convert api success but .esob not found: " + chkcvrtFilePathNm);
							intCvrt = 0;
						}
					}
					//이미 파일 생성된 경우 변환 안 함*/
					strTotalPageNo = String.valueOf(intCvrt);

					System.out.println("strTotalPageNo=  "+strTotalPageNo);
					System.out.println("ObjectId= " + objectID);
					System.out.println("intCvrt= " + intCvrt);

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

			System.out.println("strObjectIdSql -> " + strObjectIdSql);

			strObjectIdSql = strObjectIdSql.substring(1,strObjectIdSql.length() );

			System.out.println("StrObjectIdSql == " + strObjectIdSql);

			strFeedObjectId= feedObjectId.substring(1, feedObjectId.length());
			strParamWmType= paramWmType.substring(1, paramWmType.length());
			strFeedObjectIdFilePathNm= feedObjectIdFilePathNm.substring(1, feedObjectIdFilePathNm.length());

			//머지 파일 생성 후 경로가져와서 DB입력

			System.out.println("intCountCvrt= "+intCountCvrt);
			System.out.println("strFeedObjectId= "+strFeedObjectId);
			System.out.println("strParamWmType= "+strParamWmType);
			System.out.println("strFeedObjectIdFilePathNm= "+strFeedObjectIdFilePathNm);

			strMergeFilePathNm = "";

			if( null == strFeedObjectIdFilePathNm ) {            //머지 대상이 없을경우
				result.setSuccess(false);
			} else {                                //머지 대상이 있는경우


				//오늘 날짜로 폴더 생성
				Calendar cal = Calendar.getInstance();
				String dateString;
				dateString = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
				File f = new File(mergePdfPath + "\\" +dateString);

				if (!f.mkdirs())
					//System.err.println("디렉토리 생성 실패");

					//GUID 구함
					strGuid = pdao.selectGuid("");

				strMergeFilePathNm = mergePdfPath + "\\" +dateString + "\\" + strGuid + ".pdf";
				strCopyFilePathNm = mergePdfPath + "\\" +dateString + "\\" + strGuid + ".esob";
				//strMergeFilePathNm = mergePdfPath + "\\" +dateString + "\\" + "test.pdf";
				strMergeFileUrl =  adapPdfUrl + "?file=" + "/out/mergefile" + dateString + "/" + strGuid + ".esob";
				strMergedirUrl = adapPdfUrl + "?file=" + "/out/mergefile/" + dateString + "/" + URLEncoder.encode(strGuid) + ".esob";
				//strMergedirUrl = strMergedirUrl + "&ParamWmType=" + strParamWmType;

				System.out.println("strFeedObjectIdFilePathNm= " + strFeedObjectIdFilePathNm);
				System.out.println("strMergeFilePathNm= " + strMergeFilePathNm);

				// 파일이 1개면 이동
				if (intCountCvrt == 1 ) {
					copyFile(strFeedObjectIdFilePathNm, strCopyFilePathNm);
				}
				// 파일이 1개보다 크면 머지
				else if (intCountCvrt > 1) {
					epdfconvert mergeFile = new epdfconvert();
					intCvrt = mergeFile.pdfmerge(strFeedObjectIdFilePathNm, strMergeFilePathNm);
				}

				System.out.println("intCvrt= "+intCvrt);

				// 출력 횟수 업데이트
				param.setObjectId(objectID);
				dao.updatePrintCnt(param);

				//성공
				result.setSuccess(true);
				result.setFilePath(strMergedirUrl);
				result.setWatermarkInfo(strParamWmType);
			}
		} catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
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
				System.out.println("convert api skip: input file not found " + inputFilePath);
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
			System.out.println("convert api status=" + status + ", body=" + body);
			if (status >= 200 && status < 300) {
				promoteConvertedPdfAsEsob(outputDir, objectId);
				return 1;
			}
		} catch (Exception e) {
			System.out.println("convert api error: " + e.getMessage());
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
			File targetEsob = new File(outputDir + "\\" + objectId + ".esob");
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
			System.out.println("converted pdf mapped to esob: " + candidates[0].getAbsolutePath() + " -> " + targetEsob.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("promoteConvertedPdfAsEsob error: " + e.getMessage());
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


	public CommonViewerVO getPrintInfo(CommonViewerParam param) throws ParseException {
		CommonViewerVO result = new CommonViewerVO();

		try {
			String outServerFilepath = SystemConfig.getSystemConfigValue("UPDOWN_PRINT_PATH");

			if(param.getSessionUser().getAuthSite().equals("E")) {
				result.setViewerCabUrl(SystemConfig.getSystemConfigValue("VIEWER_CAB_URL_OUT"));
			}else {
				result.setViewerCabUrl(SystemConfig.getSystemConfigValue("VIEWER_CAB_URL"));
			}

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
					filePath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + fileInfo.getFilePath();
					filePathOut = outServerFilepath + fileInfo.getFileOrgNm();
//						orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + tempFileInfo.getFileNm();
				}

				//내부서버 네트워크 드라이브 원본 경로
				String orgPath = SystemConfig.getSystemConfigValue("VIEWER_NETWORK_PATH") + fileInfo.getFilePath().replaceAll("/", "\\\\");
				String sViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE");
				String oViewerServerIp = SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE");
				String fileTransId = "";
				if(param.getSessionUser().getAuthSite().equals("E")) {
					//외부서버 출력
					result.setFileOrgNm(fileInfo.getFileOrgNm());
					log.debug("filePathOut : " + filePathOut);
					result.setSuccess(true);
					//http://211.197.235.163:9001/DaVuForEG/DaViewSvc?ediauto=T&filename=$D:\\DOCS\\FILE\\general\\ff\\f2\\UA20030925_1_D_0_01.PLT

					//외부서버로 파일 Copy 요청
					JSONObject fileCall = FileUtil.callSender(sViewerServerIp, oViewerServerIp, orgPath, outServerFilepath, fileInfo.getFileOrgNm());
					//System.out.println("fileNm ================================> " + fileCall.get("fileNm"));
					fileTransId = (String)fileCall.get("fileNm");

//					String oViewerPath = orgPathOutTemp + fileCall.get("fileNm");
//					//System.out.println("oViewerPath ================================> " + oViewerPath);
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

					log.info(fileCall.toString());
				}else {
					//내부서버 출력
					result.setFileOrgNm(fileInfo.getFileOrgNm());
					log.debug("filePath : " + filePath);
					result.setSuccess(true);
				}

				//System.out.println("fileInfo.getFileOrgNm() : " + fileInfo.getFileOrgNm());
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

				if(result.isSuccess()) {
					//워터마크
					filePath = filePath.replaceAll("/", "\\\\\\\\");
//					filePath = filePath.replaceAll("a2085qdfpkgoaqow3mamhtmyn8hupqsbiqf8d1t8p6c.g4w", "111.txt");
					log.debug("replace filePath : " + filePath);

					String viewerCallUrl = "";

					if(param.getSessionUser().getAuthSite().equals("E")) {
						//viewerCallUrl = SystemConfig.getSystemConfigValue("VIEWER_URL_OUT") + outServerFilepath + fileTransId;

						viewerCallUrl = outServerFilepath + fileTransId;
						System.out.println("oViewerPath ================================> " + viewerCallUrl);

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

					System.out.println("프린트 호출 URL : " +  viewerCallUrl);
					log.info("프린트 호출 URL : " + viewerCallUrl);
					CommonViewerParam watermarkParam = new CommonViewerParam();
					watermarkParam.setUserType(param.getUserType());
					watermarkParam.setWatermarkType(param.getWatermarkType());
					result.setWatermarkInfo(getWatermarkInfo(watermarkParam, fileInfo));

					log.debug(JSONObject.fromObject(result).toString());

					//출력 횟수 업데이트
					dao.updatePrintCnt(param);
				}else {

				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}

		return result;
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
		dao.updatePrintCnt(param);
	}

	private boolean isFileApiPath(String filePath) {
		String[] fileApiPath = splitFileApiPath(filePath);
		boolean result = fileApiPath != null;
		System.out.println("[FILE_API_VIEWER] check path=" + normalizeFileApiPath(filePath) + ", matched=" + result);
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
		System.out.println("[FILE_API_VIEWER] cacheDir=" + cacheDir + ", fileName=" + fileName + ", bytes=" + bytes.length);
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
		System.out.println("[FILE_API_VIEWER] cached file=" + target.getAbsolutePath() + ", exists=" + target.isFile() + ", size=" + target.length());
		return target.getAbsolutePath();
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
		return cacheDir.replace("$", "").trim();
	}

	private String buildAdapPdfViewerUrl(String fileName) throws UnsupportedEncodingException {
		String adapPdfUrl = SystemConfig.getSystemConfigValue("ADAP_PDF_URL");
		String pdfFileUrl = SystemConfig.getSystemConfigValue("ADAP_PDF_FILE_URL");
		if (adapPdfUrl == null || adapPdfUrl.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_URL is empty");
		}
		if (pdfFileUrl == null || pdfFileUrl.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_FILE_URL is empty");
		}
		String fileUrl = pdfFileUrl.trim() + "/" + URLEncoder.encode(fileName, "UTF-8");
		return adapPdfUrl.trim() + "?file=" + URLEncoder.encode(fileUrl, "UTF-8");
	}
}
