package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import epdf.epdfconvert;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.convert.ConvertLogDao;
import kr.esob.fdms.commonlogic.fileapi.FileApiClient;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.viewer.CommonViewerParam;
import kr.esob.fdms.commonlogic.viewer.CommonViewerService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
//import kr.esob.fdms.util.epdfconvert;



@Controller
@RequestMapping("/inside/distribution/docPdfLinkRequest")
@Log4j2
public class DocPdfLinkRequestController extends AbstractController {

	@Autowired
	DocPdfLinkRequestDao dao;

	@Autowired
	CommonViewerService commonViewerService;

	@Autowired
	ConvertLogDao convertLogDao;

	private final FileApiClient fileApiClient = new FileApiClient();

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		//model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDocPdfRequest")));
		//model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDocPdfRequest")));
		//model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocPdfRequestList")));

		return "/inside/distribution/docPdfLinkRequestList";
		//return "/inside/distribution/docRequestList";
	}


	// SecureRandom 클래스를 이용한 랜덤난수 17자 생성
	private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom RANDOM = new SecureRandom();

	private String getRandomString(int length) {
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			result.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
		}
		return result.toString();
	}

	private String getConfigCd(Map<String, Object> config) {
		Object value = config.get("SYSTEM_CONFIG_CD");
		if (value == null) {
			value = config.get("system_config_cd");
		}
		return value == null ? "" : value.toString();
	}

	private String getConfigValue(Map<String, Object> config) {
		Object value = config.get("SYSTEM_CONFIG_VALUE");
		if (value == null) {
			value = config.get("system_config_value");
		}
		return value == null ? "" : value.toString();
	}

	private String toFileNameOnly(String pathOrName) {
		if (pathOrName == null || pathOrName.isEmpty()) {
			return pathOrName;
		}
		int slash = pathOrName.lastIndexOf('/');
		int backslash = pathOrName.lastIndexOf('\\');
		int idx = Math.max(slash, backslash);
		return idx >= 0 ? pathOrName.substring(idx + 1) : pathOrName;
	}
	//

	//

	@SuppressWarnings("deprecation")
	@RequestMapping(value="/selectItem2", method=RequestMethod.GET)
	public String selectItem2(
			@RequestParam("file") String file,
			@RequestParam("objectType") String objectType,
			@RequestParam("requestNo") String requestNo,
			@RequestParam("fileNo") String fileNo,
			Authentication authentication,
			Model model) throws UnsupportedEncodingException, ParseException {

		//if(!PortCheck.isPortListening()){
		//	log.info("asdasdasd");
		//	return "redirect:/";
		//}

		UserVO userVo = (UserVO) authentication.getPrincipal();
		log.info("name -> " +userVo.toString());
		//config table에서, 대상파일 경로 조희
		List<Map<String,Object>> dbConfig = dao.selectDbConfig();
		String adapDocFilePath="";
		String adapRepoPath="";
		String adapViewerPath="";
		String adapPdfPath="";
		String adapPdfUrl = "";
		String orgFileNm="";
		String cvrtFileNm="";
		String chkcvrtFilePathNm="";
		String orgFilePathNm="";
		String cvrtFilePathNm="";
		String cvrtFileUrl = "";
		String dirUrl = "";
		String adapVideoUrl = "";
		String adapVideoPath = "";
		String swDonwUrl = "";
		String adap3DPath = "";
		String adap3DUrl = "";
		String adapPostUrl = "";
		String useRedirectMultiple = "";

		String objectID = "";
		objectID = file;

		for(Map<String,Object> config : dbConfig) {
			System.out.println("Config keys and values: ");
			for (String key : config.keySet()) {
				System.out.println(key + " = " + config.get(key));
			}
			if("ADAP_ORG_FILE_PATH".equals(getConfigCd(config))) {
				adapDocFilePath = getConfigValue(config);
			}
			if("ADAP_REPO_PATH".equals(getConfigCd(config))) {
				adapRepoPath = getConfigValue(config);
			}
			if("ADAP_VIEWER_PATH".equals(getConfigCd(config))) {
				adapViewerPath = getConfigValue(config);
			}
			if("ADAP_PDF_PATH".equals(getConfigCd(config))) {
				adapPdfPath = getConfigValue(config);
			}
			if("ADAP_PDF_URL".equals(getConfigCd(config))) {
				adapPdfUrl = getConfigValue(config);
			}
			if("ADAP_VIDEO_URL".equals(getConfigCd(config))){
				adapVideoUrl = getConfigValue(config);
			}
			if("ADAP_VIDEO_PATH".equals(getConfigCd(config))){
				adapVideoPath = getConfigValue(config);
			}
			if("SW_DOWN_URL".equals(getConfigCd(config))){
				swDonwUrl = getConfigValue(config);
			}
			if("ADAP_3D_PATH".equals(getConfigCd(config))){
				adap3DPath = getConfigValue(config);
			}
			if("ADAP_3D_URL".equals(getConfigCd(config))){
				adap3DUrl = getConfigValue(config);
			}
			if("ADAP_POST_URL".equals(getConfigCd(config))){
				adapPostUrl = getConfigValue(config);
			}
			if("CV_REVISION_LIST_YN".equals(getConfigCd(config))){
				useRedirectMultiple = getConfigValue(config);
			}
		}

		// 구분해서 호출 우리뷰어호출 대상(문서, 2D), 외주뷰어(3D) + 영상
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("OBJECT_ID",objectID);
		map.put("FILE_NO", fileNo);
		log.info("objectType -> " + objectType);
		if (objectType.equals("DOC") || objectType.equals("문서")) {
			System.out.println("objectType= DOC =  " + objectType);
			orgFileNm = dao.selectFilePathNmDoc(map);
		} else if (objectType.equals("DRAWING") || objectType.equals("도면")) {
			System.out.println("objectType= Drawing = " + objectType);
			// 경로까지 넣어줘야함
			orgFileNm = dao.selectFilePathNmDrawing(map);
		} else if (objectType.equals("VIDEO") || objectType.equals("영상")) {
			System.out.println("objectType= Video = " + objectType);
			orgFileNm = dao.selectFilePathNmVideo(map);
		} else if ("SW".equalsIgnoreCase(objectType)) {
			System.out.println("objectType= sw = "+objectType);
			// 경로까지 넣어줘야함
			orgFileNm = dao.selectSwFile(map);
		} else if ("Production".equalsIgnoreCase(objectType)) {
			System.out.println("objectType= Production = "+objectType);
			// 경로까지 넣어줘야함
			orgFileNm = dao.selectProduction(map);
		} else if ("Dxf".equalsIgnoreCase(objectType) || "DXF".equalsIgnoreCase(objectType)) {
			System.out.println("objectType= Dxf = "+objectType);
			// 경로까지 넣어줘야함
			orgFileNm = dao.selectDxf(map);
		} else if ("PEERREVIEW".equalsIgnoreCase(objectType) || "PeerReview".equalsIgnoreCase(objectType)) {
			System.out.println("objectType= PeerReview = " + objectType);
			orgFileNm = dao.selectPeerReview(map);
		}
//		else if (objectType.equals("Dxf")) {
//			System.out.println("objectType= Dxf = "+objectType);
//			// 경로까지 넣어줘야함
//			orgFileNm = dao.selectDxf(map);
//		}
		else {
			// 미등록에 있으면 한번 가져와보자
//			System.out.println("yskim_test: 미등록자료 호출");

			/**
			 * 각 확장자별로 처리를 해주는 것이 좋아보임.
			 * png, jpg, jpeg, txt, hwp, pptx, ppt 등등을 제외하곤 3d 라고 간주하고 3d 뷰어로 리다이렉트 시키는 방법은 어떻습니까
			 */

			Map<String, Object> unregParam = new HashMap<String, Object>();
			unregParam.put("FILE_CD", objectID);
			unregParam.put("FILE_NO", fileNo);

			orgFileNm = dao.selectFilePathNmUnReg(unregParam);
//			commonViewerService.getViewFilePath(CommonViewerParam.builder()
//					.objectId(file)
//					.fileNo(fileNo)
//					.objectType("objectType")
//					.requestType("UNREG")
//					.build()
//			);

			objectID = objectID + "_" + fileNo;
		}

//		2023.06.29 (yskim)
		if (objectType.equals("VIDEO")){
			orgFilePathNm = /*adapViewerPath + */orgFileNm;
			System.out.println("orgFilePathNm ㅡㅡㅡㅡㅡㅡ> " + orgFilePathNm);
			System.out.println("orgFileNm ㅡㅡㅡㅡㅡㅡ> " + orgFileNm);

			String disposableKey = getRandomString(17);

			Date now = new Date();
			// DOCS_VIEWER_KEY table removed: skip key persistence.


			dirUrl = adapVideoUrl + "?file=" + "/out/video/" + URLEncoder.encode(objectID) + ".mp4&user_name=" + userVo.getUserCd() + "&disposable_key=" + disposableKey;
		}else {

			orgFilePathNm = /*adapViewerPath + */orgFileNm;
			System.out.println("orgFilePathNm ㅡㅡㅡㅡㅡㅡ> " + orgFilePathNm);
			System.out.println("orgFileNm ㅡㅡㅡㅡㅡㅡ> " + orgFileNm);
			//cvrtFilePathNm = adapPdfPath + orgFileNm+".pdf";
			//cvrtFileUrl =  adapPdfUrl + "?file=" + "/out/destfile" + orgFileNm + ".esob";
			//dirUrl = adapPdfUrl + "?file=" + "/out/destfile" + URLEncoder.encode(orgFileNm) + ".esob";
			System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

			cvrtFilePathNm = adapPdfPath + "\\" + objectID + ".pdf";
			System.out.println("adapPdfPath ㅡㅡㅡㅡㅡㅡㅡㅡㅡ>" + adapPdfPath);
			System.out.println("objectID ㅡㅡㅡㅡㅡㅡㅡㅡㅡ>" + objectID);

			chkcvrtFilePathNm = cvrtFilePathNm;
			cvrtFileUrl = buildAdapPdfViewerUrl(adapPdfUrl, objectID);
			System.out.println("adapPdfUrl ㅡㅡㅡㅡㅡㅡㅡㅡㅡ>" + adapPdfUrl);
			System.out.println("cvrtFileUrl ㅡㅡㅡㅡㅡㅡㅡㅡㅡ>" + cvrtFileUrl);

			System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
			//키 생성 & DOCS_VIEWER_KEY에 insert
			String disposableKey = getRandomString(17);

			Date now = new Date();
			// DOCS_VIEWER_KEY table removed: skip key persistence.

			// CollabView thread에서 사용자cd가 아닌 이름을 보여주기 위한 인자전달.
			String encodedUserName = URLEncoder.encode(userVo.getUsername(), StandardCharsets.UTF_8.toString());

//			dirUrl = adapPdfUrl + "?file=" + "/out/destfile/" + URLEncoder.encode(objectID) + ".esob&user_name=" + userVo.getUserCd() + "&disposable_key=" + disposableKey + "&object_ID=" + objectID ;
			dirUrl = cvrtFileUrl + "&user_name=" + encodedUserName + "&disposable_key=" + disposableKey + "&object_ID=" + objectID  ;
		}

		if ("SW".equalsIgnoreCase(objectType)) {
			cacheSwFileApiPdfForViewer(orgFileNm, adapPdfPath, objectID);
		}

		String fileDownloadUrl = "";


		int intCvrt = 10;
		//epdfconvert cvrtFile = new epdfconvert();
		//intCvrt = cvrtFile.pdfconvert(orgFilePathNm,cvrtFilePathNm);

		System.out.println("file exists check: " + chkcvrtFilePathNm);
		File fileExist = new File(chkcvrtFilePathNm);


		System.out.println("fileExist -> " + fileExist.exists());
		if (fileExist.isFile()) {
			System.out.println("cvrtFilePathNm exists : " + chkcvrtFilePathNm);
			map.put("PATH_NM", fileExist.getPath());
			map.put("ORG_FILE_NM", fileExist.getName());
			map.put("FILE_SIZE", fileExist.length());
			map.put("USER_ID", userVo.getUserCd());
			map.put("CONVERT_TYPE", "2D");
			map.put("RESULT_CODE", intCvrt);
			// convert_log table removed: skip convert log insert.
		} else {
			try{
				System.out.println("fileExist.isFile() false and try");
				// 여기서 경로를 보고 3D인지 2D인지만 분기해줘서 파일 돌려줌
				// 그리고 나서 node 서버 리다이렉트 시켜주면 됨 (경로 중간에 확인해야겠지요?)
				System.out.println("ㅡㅡ1");
				System.out.println("cvrtFile");

//				System.out.println("이게 뭔데 -> " + orgFilePathNm );
//				System.out.println("이게 뭔데2 -> " + orgFileNm );
				System.out.println("ㅡㅡ2");
				File fileOrgFilePath = new File(orgFileNm);
				System.out.println("fileOrgFilePath exists: " + fileOrgFilePath.exists());
				System.out.println("fileOrgFilePath: " + fileOrgFilePath);
				System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ3");
				if(fileOrgFilePath.exists()){
					System.out.println("기존 파일 경로 -> " + fileOrgFilePath.getPath());
					System.out.println("변환 파일 경로 -> " + cvrtFilePathNm);

					map.put("PATH_NM", fileOrgFilePath.getPath());
					map.put("ORG_FILE_NM", fileOrgFilePath.getName());
					map.put("FILE_SIZE", fileOrgFilePath.length());
					map.put("USER_ID", userVo.getUserCd());

					for(Map<String,Object> config : dbConfig) {
						String path = getConfigValue(config);
						String configCD = getConfigCd(config);

						// 해당 파일의 경로와 디비 상에 있는 경로가 일치할 경우
						if(path.equals(fileOrgFilePath.getParent())){

							// 3D 라면
							if(configCD.equals("3D_FILE_PATH")){
								log.info("3d 호출중");

								String path2 = FileUtil.filePath(fileOrgFilePath.getName());
								String fileNameWithOutExtention = FileUtil.fileName(fileOrgFilePath.getName());
								String redirectParameter = path2 + "/" +fileNameWithOutExtention;
//								redirectParameter = redirectParameter.replaceAll("/", "\\");
								log.info("redirectParameter -> " + redirectParameter);
								File file2 = new File(adap3DPath + "\\" + redirectParameter);
								if(file2.exists()){
									log.info("파일 이미 존재");
									map.put("CONVERT_TYPE", "3D");
									map.put("RESULT_CODE", intCvrt);
									// convert_log table removed: skip convert log insert.
									System.out.println("result -> " + intCvrt);
//									return "redirect:http://collabhub.esob.kr:7442/VIZWeb3D/index.html?file="+ redirectParameter;
//									return "redirect:http://demo.esob.kr:7442/VIZWeb3D/index.html?file="+ redirectParameter;
//									return "redirect:http://192.168.0.113:3509/VIZWeb3D/index.html?file="+ redirectParameter;
									String returnUrl = "redirect:"+ adap3DUrl + "?file=" + redirectParameter;
									return returnUrl;
								}
								epdfconvert cvrtFile = new epdfconvert();
								intCvrt = cvrtFile.convert3D(fileOrgFilePath.getPath(), adap3DPath);

								map.put("CONVERT_TYPE", "3D");
								map.put("RESULT_CODE", intCvrt);
								// convert_log table removed: skip convert log insert.
								System.out.println("result -> " + intCvrt);
//								return "redirect:http://collabhub.esob.kr:7442/VIZWeb3D/index.html?file="+ redirectParameter;
//								return "redirect:http://demo.esob.kr:7442/VIZWeb3D/index.html?file="+ redirectParameter;
//								return "redirect:http://192.168.0.113:3509/VIZWeb3D/index.html?file="+ redirectParameter;

								String returnUrl = "redirect:"+ adap3DUrl + "?file=" + redirectParameter;

								Map<String, String> paramMap = new HashMap<>();
								System.out.println("objectId: " + objectID);
								System.out.println("objectId2: " + objectID);
								paramMap.put("OBJECT_ID", objectID);

								if (objectType.equals("DRAWING") || objectType.equals("도면")) {
									paramMap.put("table", "DOCS_DRAWING");
								}

								String insertDt = dao.selectInsertDt(paramMap);
								String formattedInsertDt = formatInsertDt(insertDt);
								returnUrl += "&insert_dt=" + formattedInsertDt;

								log.info("final dirUrl: " + dirUrl);
								return returnUrl;
							}

							// 영상 이라면
							else if(configCD.equals("VIDEO_PATH")){
								System.out.println(" 영상 호출중 ");
								System.out.println(objectID);
								System.out.println(adapVideoPath);
								System.out.println("fileOrgFilePath >>> " + fileOrgFilePath);
								System.out.println("fileOrgFilePath.getPath() >>> " + fileOrgFilePath.getPath());
								System.out.println("adapVideoPath +objectID +\".mp4\" >>>>" + adapVideoPath +"/" +objectID +".mp4");
								Files.copy(Paths.get(fileOrgFilePath.getPath()), Paths.get(adapVideoPath +"/" +objectID +".mp4"), StandardCopyOption.REPLACE_EXISTING);

								map.put("CONVERT_TYPE", "VIDEO");
								map.put("RESULT_CODE", "1");
								// convert_log table removed: skip convert log insert.
							}
							// 2D 라면
							else if(configCD.equals("2D_FILE_PATH") || configCD.equals("DOCUMENT_PATH")||configCD.equals("PRODUCTION_PATH")||configCD.equals("DXF_PATH")||configCD.equals("SW_PATH")){
								intCvrt = convertToViewerPdf(fileOrgFilePath, cvrtFilePathNm, adapPdfPath, objectID);
								map.put("CONVERT_TYPE", "2D");
								map.put("RESULT_CODE", intCvrt);
								// convert_log table removed: skip convert log insert.
							}

							// 소프트 웨어
							else if(false && configCD.equals("SW_PATH")){
								intCvrt = 20;
								String onlyWindowPath = fileOrgFilePath.getPath().replaceAll("/", "\\");
								System.out.println("원래 확인중 -> " + fileOrgFilePath.getPath());
								System.out.println("경로 확인중 -> " + onlyWindowPath);
//								fileDownloadUrl = "http://collabhub.esob.kr:80/download/?fileName="+orgFileNm;
//								fileDownloadUrl = "http://collabhub.esob.kr:80/download/?fileName="+orgFileNm;
//								fileDownloadUrl = "http://demo.esob.kr:80/download/?fileName="+orgFileNm;
								fileDownloadUrl = swDonwUrl + "download/?fileName="+orgFileNm;

								UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(fileDownloadUrl).build();
								return "redirect:" + uriComponents.encode().toUri();
//								fileDownloadUrl = "http://collabhub.iptime.org:80/download/?fileName="+uriComponents.encode().toUri();
							}
						}
						// 23.06.29 (yskim) unreg인 경우
						else if (fileOrgFilePath.getPath().contains(path)) {
							if(configCD.equals("UNREG_FILE_PATH")) {
								System.out.println("Unreg convert in");
								intCvrt = convertToViewerPdf(fileOrgFilePath, cvrtFilePathNm, adapPdfPath, objectID);
								map.put("CONVERT_TYPE", "UNREG");
								map.put("RESULT_CODE", intCvrt);
								// convert_log table removed: skip convert log insert.

								System.out.println("Unreg convert out" + intCvrt);
							}

						} else {
							// 나머지 Production, SW 등등..
						}
					}
				} else {
					// 파일이 존재하지 않는다면
//					System.out.println("로그 체크1");
//					intCvrt = cvrtFile.pdfconvert(orgFilePathNm,cvrtFilePathNm);
				}

			}catch(Exception e){
				System.out.println(e);
			}
		}


		System.out.println("objectType= "+objectType);
		System.out.println("ObjectId= "+objectID);
		System.out.println("intCvrt= " + intCvrt);
		System.out.println("orgFilePathNm= "+orgFilePathNm);
		System.out.println("cvrtFilePathNm= "+cvrtFilePathNm);
		System.out.println("adapViewerPath= "+adapViewerPath);
		System.out.println("adapPdfPath= "+adapPdfPath);
		System.out.println("cvrtFileUrl= "+cvrtFileUrl);
		System.out.println("dirUrl= "+dirUrl);


		if (intCvrt == 0) {
			model.addAttribute("convertFailRestricted", "Y");
			return "/inside/distribution/docConvertFail";
		} else {
			CommonViewerParam param = new CommonViewerParam();
			param.setObjectId(objectID);
			param.setRequestNo(requestNo);
			param.setFileNo(fileNo);
			param.setFileNo(fileNo);
			log.info(objectID);
			log.info(requestNo);
			log.info(fileNo);

			Date downDate = new Date();
//			commonViewerService.updatePrintCnt(param);

			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("OBJECT_ID", objectID);
			paramMap.put("object_id", objectID);

			if (objectType.equals("DOC") || objectType.equals("문서")) {
				paramMap.put("table", "docs_document");
			} else if (objectType.equals("DRAWING") || objectType.equals("도면")) {
				paramMap.put("table", "docs_drawing");
			} else if (objectType.equals("Production")) {
				paramMap.put("table", "docs_product_document");
			}else if (objectType.equals("Dxf")) {
				paramMap.put("table", "docs_dxf_document");
			} else if ("PEERREVIEW".equalsIgnoreCase(objectType) || "PeerReview".equalsIgnoreCase(objectType)) {
				paramMap.put("table", "docs_peerreview");
			}

			String insertDt = dao.selectInsertDt(paramMap);
			String formattedInsertDt = "";
			if (insertDt != null && !insertDt.isEmpty()) {
				formattedInsertDt = formatInsertDt(insertDt);
				dirUrl += "&insert_dt=" + formattedInsertDt;
			}

			log.info("final dirUrl: " + dirUrl);

			// DOCS_VIEWER_LOG에 viewer 정보 추가
			dao.insertViewerData(objectID, userVo.getUserCd(), downDate);


//			// DOCS_VIEWER_KEY에 있는 key 삭제
//			dao.deleteKey(objectID,disposableKey);

			String distributionType = "";
			String drawingNo = "";
			String fileName = "";
			String revision = "";

			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put("OBJECT_ID", objectID);
			paramsMap.put("FILE_NO", fileNo);

			List<Map<String, Object>> revisionList = new ArrayList<>();

			List<Map<String, Object>> revisions =  Collections.emptyList();
			System.out.println("useRedirectMultiple: " + useRedirectMultiple);

			if (objectType.equals("DOC") || objectType.equals("문서")) {
				distributionType = "IOC";
				drawingNo = dao.selectDocumentNoDoc(paramsMap);
				fileName = dao.selectFileNmDoc(paramsMap);
				revision = null;
			} else if (objectType.equals("DRAWING") || objectType.equals("도면") || objectType.equals("도면/공정서")) {
				distributionType = "FunctionCode";
				drawingNo = dao.selectDrawingNoDrawing(paramsMap);
				fileName = dao.selectFileNmDrawing(paramsMap);
				revision = dao.selectRevisionDrawing(paramsMap);
				System.out.println("distributionType: " + distributionType);
				System.out.println("drawingNo: " + drawingNo);
				System.out.println("fileName: " + fileName);
				System.out.println("revision: " + revision);

				if("true".equalsIgnoreCase(useRedirectMultiple)){
					revisions = dao.selectRevisionListDrawing(paramsMap);
				}
				if (revisions != null) {
					for (Map<String, Object> r : revisions) {
						System.out.println(r);
					}
				} else {
					System.out.println("revisions is null");
				}

				if (revisions != null && revisions.size() > 1) {
					// 여러 리비전이 존재하면 revisionList 채움
					for (Map<String, Object> rev : revisions) {
						Map<String, Object> revMap = new HashMap<>();
						revMap.put("objectID", rev.get("OBJECT_ID"));
						revMap.put("filePath", rev.get("FILE_PATH"));
						revMap.put("revision", rev.get("REV_NO"));
						revisionList.add(revMap);
					}
				}

			} else if ("Production".equalsIgnoreCase(objectType) || objectType.equals("생산기술자료")) {
				distributionType = "MRB";
				drawingNo = dao.selectDrawingNoCP(paramsMap);
				fileName = dao.selectFileNmCP(paramsMap);
				revision = dao.selectRevisionCP(paramsMap);

				if("true".equalsIgnoreCase(useRedirectMultiple)) {
					revisions = dao.selectRevisionListCP(paramsMap);
				}
				if (revisions != null && revisions.size() > 1) {
					// 여러 리비전이 존재하면 revisionList 채움
					for (Map<String, Object> rev : revisions) {
						Map<String, Object> revMap = new HashMap<>();
						revMap.put("objectID", rev.get("OBJECT_ID"));
						revMap.put("filePath", rev.get("FILE_PATH"));
						revMap.put("revision", rev.get("REV_NO"));
						revisionList.add(revMap);
					}
				}

			} else if ("Dxf".equalsIgnoreCase(objectType) || "DXF".equalsIgnoreCase(objectType)) {
				distributionType = "PMPCB";
				drawingNo = dao.selectDrawingNoDXF(paramsMap);
				fileName = dao.selectFileNmDXF(paramsMap);
				revision = dao.selectRevisionDXF(paramsMap);

				if("true".equalsIgnoreCase(useRedirectMultiple)) {
					revisions = dao.selectRevisionListDXF(paramsMap);
				}
				if (revisions != null && revisions.size() > 1) {
					// 여러 리비전이 존재하면 revisionList 채움
					for (Map<String, Object> rev : revisions) {
						Map<String, Object> revMap = new HashMap<>();
						revMap.put("objectID", rev.get("OBJECT_ID"));
						revMap.put("filePath", rev.get("FILE_PATH"));
						revMap.put("revision", rev.get("REV_NO"));
						revisionList.add(revMap);
					}
				}
			} else if ("PEERREVIEW".equalsIgnoreCase(objectType) || "PeerReview".equalsIgnoreCase(objectType)) {
				distributionType = "PEERREVIEW";
				drawingNo = dao.selectPeerReviewNo(paramsMap);
				fileName = dao.selectFileNmPeerReview(paramsMap);
				revision = null;

			}
			// 2개 이상의 리비전을 가진 파일이라면, 전부 전송
			if ("SW".equalsIgnoreCase(objectType)) {
				distributionType = "CCB";
				drawingNo = dao.selectSwNo(paramsMap);
				fileName = dao.selectFileNmSW(paramsMap);
				revision = dao.selectRevisionSW(paramsMap);
			}

			String historyFileName = (fileName != null && !fileName.isEmpty()) ? fileName : orgFileNm;
			historyFileName = toFileNameOnly(historyFileName);
			dao.insertViewPrintHistory(
					distributionType,
					drawingNo,
					objectID,
					historyFileName,
					revision,
					userVo.getUserId(),
					userVo.getUserNm(),
					"VIEWING",
					downDate
			);

			String viewerAuthority = "77";
			if ("PEERREVIEW".equalsIgnoreCase(distributionType)) {
				Map<String, Object> authorityInfo = dao.selectPeerReviewAuthorityInfo(paramsMap);
				String loginUserNm = Optional.ofNullable(userVo.getUserNm()).orElse("").trim();
				String insertUserNm = Optional.ofNullable(authorityInfo)
						.map(m -> m.get("INSERT_USER_NM") != null ? m.get("INSERT_USER_NM") : m.get("insert_user_nm"))
						.map(String::valueOf)
						.orElse("")
						.trim();
				String approver = Optional.ofNullable(authorityInfo)
						.map(m -> m.get("APPROVER") != null ? m.get("APPROVER") : m.get("approver"))
						.map(String::valueOf)
						.orElse("");

				boolean isRegistrant = !loginUserNm.isEmpty() && loginUserNm.equalsIgnoreCase(insertUserNm);
				boolean isApprover = Arrays.stream(approver.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.anyMatch(id -> id.equalsIgnoreCase(loginUserNm));

				log.info("[PeerReviewAuth] loginUserNm(getUserNm)={}", loginUserNm);
				log.info("[PeerReviewAuth] insertUserNm={}, approver={}", insertUserNm, approver);
				log.info("[PeerReviewAuth] isRegistrant={}, isApprover={}", isRegistrant, isApprover);

				viewerAuthority = (isRegistrant || isApprover) ? "112" : "111";
				log.info("[PeerReviewAuth] resolved viewerAuthority={}", viewerAuthority);
			}

			if (!revisionList.isEmpty()) {
				Map<String, Object> revisionParams  = new HashMap<>();
				revisionParams .put("authority", viewerAuthority);
				revisionParams .put("userName", userVo.getUserNm());
				revisionParams .put("userID", userVo.getUserId());
				revisionParams .put("revisionList", revisionList);
				revisionParams.put("objectID", objectID);
				revisionParams.put("finalURL", dirUrl); // 최신 리비전 기준 URL
				revisionParams.put("releasedToRIWatermarkYn", getReleasedToRIWatermarkYn(userVo));//260630 kt1b
				model.addAttribute("params", revisionParams );
				return handleRedirectMultiple(revisionParams , model);
			}

			Map<String, String> params = new HashMap<>();
			params.put("finalURL", dirUrl);
			params.put("url", adapPostUrl);
			params.put("userName", userVo.getUserNm());
			params.put("userID", userVo.getUserId());
			params.put("objectID", objectID);
			params.put("requestNo", requestNo);
			params.put("distributionType", distributionType);
			params.put("orgFileNm", orgFileNm);
			params.put("drawingNo", drawingNo);
			params.put("fileName", toFileNameOnly(fileName));
			params.put("revision", revision);
			params.put("insertDt", formattedInsertDt);
			params.put("authority", viewerAuthority);
			params.put("filePath","");
			params.put("releasedToRIWatermarkYn", getReleasedToRIWatermarkYn(userVo));//260630 kt1b
			return handleRedirect(params, model);

//			return "redirect:"+dirUrl;
		}
		//return "redirect:http://192.168.190.1:7442/web/viewer.html?file="+adapViewerPath;

	}

	private String handleRedirect(Map<String, String> params, Model model) {
		model.addAttribute("params", params);

		System.out.println("POST REQUEST PARAMETERS:");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}

		return "/inside/distribution/redirectPost";
	}

	private int convertToViewerPdf(File inputFile, String targetPdfPath, String outputDir, String objectId) {
		try {
			if (inputFile == null || !inputFile.isFile()) {
				return 0;
			}

			File targetFile = new File(targetPdfPath);
			File parent = targetFile.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				return 0;
			}

			String lowerName = inputFile.getName().toLowerCase(Locale.ROOT);
			if (lowerName.endsWith(".pdf")) {
				Files.copy(inputFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return targetFile.isFile() ? 1 : 0;
			}

			String convertedPdfPath = requestConvertServer(inputFile, outputDir, objectId);
			if (convertedPdfPath == null || convertedPdfPath.trim().isEmpty()) {
				return 0;
			}

			File convertedFile = new File(convertedPdfPath);
			if (!convertedFile.isFile()) {
				return 0;
			}

			Files.copy(convertedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return targetFile.isFile() ? 1 : 0;
		} catch (Exception e) {
			log.error("Viewer PDF convert failed. input={}, target={}", inputFile, targetPdfPath, e);
			return 0;
		}
	}

	private String requestConvertServer(File inputFile, String outputDir, String objectId) {
		HttpURLConnection connection = null;
		DataOutputStream requestStream = null;
		try {
			String endpoint = SystemConfig.getSystemConfigValue("CONVERT_SERVER_URL");
			if (endpoint == null || endpoint.trim().isEmpty()) {
				endpoint = "http://localhost:9001/api/internal/convert";
			}

			String boundary = "----FDMS-VIEWER-" + System.currentTimeMillis();
			connection = (HttpURLConnection) new URL(endpoint).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			requestStream = new DataOutputStream(connection.getOutputStream());
			writeFormField(requestStream, boundary, "outputDir", outputDir);

			String fileName = inputFile.getName();
			int dot = fileName.lastIndexOf('.');
			String ext = dot > -1 ? fileName.substring(dot) : "";
			writeFilePart(requestStream, boundary, "files", objectId + ext, inputFile);

			requestStream.writeBytes("--" + boundary + "--\r\n");
			requestStream.flush();

			int status = connection.getResponseCode();
			if (status >= 200 && status < 300) {
				return findLatestConvertedPdf(outputDir, objectId);
			}
			log.error("Convert server failed. status={}, endpoint={}", status, endpoint);
		} catch (Exception e) {
			log.error("Convert server request failed. input={}", inputFile, e);
		} finally {
			try {
				if (requestStream != null) requestStream.close();
			} catch (Exception ignored) {}
			if (connection != null) connection.disconnect();
		}
		return null;
	}

	private void writeFormField(DataOutputStream stream, String boundary, String name, String value) throws Exception {
		stream.writeBytes("--" + boundary + "\r\n");
		stream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
		stream.write(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
		stream.writeBytes("\r\n");
	}

	private void writeFilePart(DataOutputStream stream, String boundary, String fieldName, String fileName, File file) throws Exception {
		stream.writeBytes("--" + boundary + "\r\n");
		stream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
		stream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
		try (FileInputStream inputStream = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				stream.write(buffer, 0, read);
			}
		}
		stream.writeBytes("\r\n");
	}

	private String findLatestConvertedPdf(String outputDir, String objectId) {
		try {
			File dir = new File(outputDir);
			File[] candidates = dir.listFiles((parent, name) -> {
				String lower = name.toLowerCase(Locale.ROOT);
				return lower.startsWith(objectId.toLowerCase(Locale.ROOT) + "_") && lower.endsWith(".pdf");
			});
			if (candidates == null || candidates.length == 0) {
				return null;
			}
			Arrays.sort(candidates, (left, right) -> Long.compare(right.lastModified(), left.lastModified()));
			return candidates[0].getAbsolutePath();
		} catch (Exception e) {
			return null;
		}
	}

	private String handleRedirectMultiple(Map<String, Object> params, Model model) {
		
		model.addAttribute("params", params);

		System.out.println("POST REQUEST PARAMETERS:");
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}

		return "/inside/distribution/redirectPostMultiple";
	}


	//@RequestMapping(value="/selectItem", method=RequestMethod.GET)
	@RequestMapping(value="/selectItem")
	public String test(@RequestParam(value="file", defaultValue="") String file) {

		String strFile = "";
		//strFile = URLDecoder.decode(file, StandardCharsets.UTF_8.toString());
		//strFile = file;
		strFile = URLDecoder.decode(file);

		System.out.println(file);
		System.out.println(strFile);


		//* 두 번째 인자를 .pdf로 라이브러리 호출하지만 실제 파일은 .esob이 생김
		String strDocumentPath1 = "C:\\Users\\nobut\\esob\\TG\\OUT\\orgFile\\"+strFile;
		//String strPdfPath = "C:\\workspaceStartupHub\\OUT\\destFile\\"+strFile+".pdf";
		String strPdfPath = "C:\\Users\\nobut\\esob\\TG\\collabview\\OUT\\destFile\\"+strFile+".pdf";
		//String strPdfViewPath =  "/out/destfile/"+strFile+".pdf";
		//String strPdfPath = "C:\\workspaceStartupHub\\vServer\\pdfview\\OUT\\destFile\\"+strFile+".esob";
		String strPdfViewPath =  "/out/destfile/"+strFile+".esob";

		//strPdfViewPath = URLEncoder.encode(strPdfViewPath, "UTF-8");
		strPdfViewPath = URLEncoder.encode(strPdfViewPath);

		//EpdfConvert cvrtFile = new EpdfConvert();
		epdfconvert cvrtFile = new epdfconvert();
		int intCvrt = cvrtFile.pdfconvert(strDocumentPath1,strPdfPath);

		System.out.println(" 변환 전 : " +strDocumentPath1);
		System.out.println(" 변환 후 : " +strPdfPath);

		System.out.println(strFile + " 변환 결과 : " +intCvrt);

		return "redirect:http://192.168.0.107:7442/web/viewer.html?file="+strPdfViewPath;
	}

	//	@RequestMapping("/selectList")
//	public @ResponseBody GridResultVO selectList(DocPdfRequestParam param) throws Exception {
	//service.setSearchAllParam(param);
	//GridResultVO result = commonSelectList(param, service);
	//return result;
//	}
	public String formatInsertDt(String insertDt) {
		if (insertDt == null) {
			throw new IllegalArgumentException("insertDt cannot be null");
		}

		// 두 가지 포맷 정의: 마이크로초 포함 / 미포함
		DateTimeFormatter originalFormatWithMicros = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
		DateTimeFormatter originalFormatWithoutMicros = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yy.MM.dd");

		LocalDateTime dateTime;

		// 입력값이 마이크로초를 포함하는지 여부에 따라 처리
		try {
			dateTime = LocalDateTime.parse(insertDt, originalFormatWithMicros);
		} catch (Exception e) {
			dateTime = LocalDateTime.parse(insertDt, originalFormatWithoutMicros);
		}

		return dateTime.format(targetFormat);
	}

	@SuppressWarnings("deprecation")
	@RequestMapping(value="/pdfConvert", method=RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> pdfConverter(
			@RequestParam("file") String file,
			@RequestParam("objectType") String objectType,
			@RequestParam("requestNo") String requestNo,
			@RequestParam("fileNo") String fileNo,
			@RequestParam("userId") String userId,
			@RequestParam("filePath") String filePath,
			Model model) throws UnsupportedEncodingException, ParseException {

		//config table에서, 대상파일 경로 조희
		List<Map<String,Object>> dbConfig = dao.selectDbConfig();
		String adapPdfPath="";
		String orgFileNm="";
		String chkcvrtFilePathNm="";
		String orgFilePathNm="";
		String cvrtFilePathNm="";

		String objectID = "";
		objectID = file;

		for(Map<String,Object> config : dbConfig) {
			if("ADAP_PDF_PATH".equals(getConfigCd(config))) {
				adapPdfPath = getConfigValue(config);
//				adapPdfPath = "C:\\Users\\nobut\\esob\\TG\\collabview\\public\\OUT\\destFile";

			}
		}

		// 구분해서 호출 우리뷰어호출 대상(문서, 2D), 외주뷰어(3D) + 영상
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("OBJECT_ID",objectID);
		orgFileNm = filePath;

//		2023.06.29 (yskim)
		orgFilePathNm = /*adapViewerPath + */orgFileNm;
		System.out.println("pdfConverter.orgFilePathNm: " + orgFilePathNm);
		System.out.println("pdfConverter.orgFileNm: " + orgFileNm);
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

		cvrtFilePathNm = adapPdfPath + "\\" + objectID + ".pdf";
		System.out.println("pdfConverter.adapPdfPath: " + adapPdfPath);
		System.out.println("pdfConverter.objectID: " + objectID);

		chkcvrtFilePathNm = adapPdfPath + "\\" + objectID + ".esob";

		//키 생성 & DOCS_VIEWER_KEY에 insert
		String disposableKey = getRandomString(17);

		Date now = new Date();

		// ****임시 주석처리****
		// dao.insertKey(disposableKey, objectID, now);

		int intCvrt = 10;

		System.out.println("converted file path: " + chkcvrtFilePathNm);
		File fileExist = new File(chkcvrtFilePathNm);


		System.out.println("converted file exist -> " + fileExist.exists());
		if (fileExist.isFile()) {
			map.put("PATH_NM", fileExist.getPath());
			map.put("ORG_FILE_NM", fileExist.getName());
			map.put("FILE_SIZE", fileExist.length());
			map.put("USER_ID", userId);
			map.put("CONVERT_TYPE", "2D");
			map.put("RESULT_CODE", intCvrt);
			// convert_log table removed: skip convert log insert.
		} else {
			try{
				System.out.println("fileExist.isFile() false and try");
				epdfconvert cvrtFile = new epdfconvert();

				File fileOrgFilePath = new File(orgFileNm);
				if(fileOrgFilePath.exists()){
					System.out.println("기존 파일 경로: " + fileOrgFilePath.getPath());
					System.out.println("변환 파일 경로(.esob): " + cvrtFilePathNm);

					map.put("PATH_NM", fileOrgFilePath.getPath());
					map.put("ORG_FILE_NM", fileOrgFilePath.getName());
					map.put("FILE_SIZE", fileOrgFilePath.length());
					map.put("USER_ID", userId);

					for(Map<String,Object> config : dbConfig) {
						String path = getConfigValue(config);
						String configCD = getConfigCd(config);

						// 해당 파일의 경로와 디비 상에 있는 경로가 일치할 경우
						if(path.equals(fileOrgFilePath.getParent())){
							// 2D 라면
							if(configCD.equals("2D_FILE_PATH") || configCD.equals("DOCUMENT_PATH")||configCD.equals("PRODUCTION_PATH")||configCD.equals("DXF_PATH")){
								intCvrt = cvrtFile.pdfconvert(fileOrgFilePath.getPath(), cvrtFilePathNm);
								map.put("CONVERT_TYPE", "2D");
								map.put("RESULT_CODE", intCvrt);
								// convert_log table removed: skip convert log insert.
							}
						}
						else {
							// 나머지 Production, SW 등등..
							log.info("checking dbConfig..");
						}
					}
				} else {
					// 파일이 존재하지 않는다면
					log.info("file doesn't exist");
				}

			}catch(Exception e){
				System.out.println(e);
			}
		}

		System.out.println("objectType= "+objectType);
		System.out.println("ObjectId= "+objectID);
		System.out.println("intCvrt= " + intCvrt);
		System.out.println("orgFilePathNm= "+orgFilePathNm);
		System.out.println("cvrtFilePathNm= "+cvrtFilePathNm);
		System.out.println("adapPdfPath= "+adapPdfPath);

		Map<String, Object> result = new HashMap<>();
		if (intCvrt == 0) {
			log.info("convertfail");
			result.put("status", "convertfail");
		} else {
			log.info(objectID);
			log.info(requestNo);
			log.info(fileNo);

			log.info("suceess");
			result.put("status", "success");
			result.put("cvrtFilePathNm", cvrtFilePathNm);
		}
		return result;
	}
	
	private String buildAdapPdfViewerUrl(String adapPdfUrl, String objectId) throws UnsupportedEncodingException {
		String pdfFileUrl = SystemConfig.getSystemConfigValue("ADAP_PDF_FILE_URL");
		if (adapPdfUrl == null || adapPdfUrl.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_URL is empty");
		}
		if (pdfFileUrl == null || pdfFileUrl.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_FILE_URL is empty");
		}
		String fileName = URLEncoder.encode(objectId + ".pdf", StandardCharsets.UTF_8.toString());
		String fileUrl = pdfFileUrl.trim() + "/" + fileName;
		return adapPdfUrl.trim() + "?file=" + URLEncoder.encode(fileUrl, StandardCharsets.UTF_8.toString());
	}

	private void cacheSwFileApiPdfForViewer(String filePathNm, String adapPdfPath, String objectId) {
		String[] fileApiPath = splitFileApiPath(filePathNm);
		if (fileApiPath == null) {
			System.out.println("[FILE_API_VIEWER_SELECT_ITEM2] skip. filePathNm=" + normalizeFileApiPath(filePathNm));
			return;
		}
		if (adapPdfPath == null || adapPdfPath.trim().isEmpty()) {
			throw new IllegalStateException("ADAP_PDF_PATH is empty");
		}
		String targetFileName = objectId + ".pdf";
		File dir = new File(adapPdfPath.replace("$", "").trim());
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("Viewer cache directory cannot be created: " + dir.getAbsolutePath());
		}
		File target = new File(dir, targetFileName);
		if (target.isFile() && target.length() > 0) {
	System.out.println("[FILE_API_VIEWER_SELECT_ITEM2] cache hit=" + target.getAbsolutePath());
	return;
}
		byte[] bytes = fileApiClient.download(fileApiPath[1], fileApiPath[0]);
		try {
			Files.write(target.toPath(), bytes);
		} catch (Exception e) {
			throw new IllegalStateException("Viewer cache write failed: " + target.getAbsolutePath(), e);
		}
		System.out.println("[FILE_API_VIEWER_SELECT_ITEM2] cached file=" + target.getAbsolutePath()
				+ ", sourceFolder=" + fileApiPath[0]
				+ ", sourceFile=" + fileApiPath[1]
				+ ", exists=" + target.isFile()
				+ ", size=" + target.length());
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

private String getReleasedToRIWatermarkYn(UserVO userVo) {
    return "Y";
}
}
