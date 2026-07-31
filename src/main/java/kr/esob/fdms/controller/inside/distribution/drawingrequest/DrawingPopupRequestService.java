package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.convert.ConvertLogDao;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.commonlogic.distribution.model.RequestParam;
import kr.esob.fdms.commonlogic.distribution.model.DrawingInfoVO;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StoragePathUtils;
import kr.esob.fdms.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.net.URLEncoder;

@Service
@Log4j2
public class DrawingPopupRequestService implements CommonService{

    @Inject
    DrawingRequestDao dao;

    @Autowired
    DocPdfLinkRequestDao pdfDao;

    @Autowired
    ConvertLogDao convertLogDao;

    @Override
    public List selectList(Object param) {
        List<DrawingRequestVO> rtnList = dao.selectPopupListInside(param);
        return rtnList;
    }

    @Override
    public int selectListCount(Object param) {
        return dao.selectListCount(param);
    }

//	public void updateList(Object param) {
//		dao.updateList(param);
//	}

    public void deleteList(Object param) {

    }

    public void setSearchAllParam(DrawingRequestParam param) {
        if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
            Gson gson = new Gson();
            param.setDrawingList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DrawingInfoVO>>() {}.getType()));
        }
    }


    // 2020.07.10 기범추가( 등록 )
    public ResultVO saveDrawingRegisterFileX2(MultipartHttpServletRequest request) throws Exception {
        String isNewRevision = request.getParameter("isNewRevision");

        ResultVO resultVo = new ResultVO();
        MultipartFile file = request.getFile("file");

        String drawingType = "2D";
        String distributionPoint = request.getParameter("distributionPoint");
        String objectId = RandomStringGenerator.generateRandomString(32);
        DrawingRegisterPopupParam drawingRegisterPopupParam = new DrawingRegisterPopupParam();

        if (isNewRevision != null && isNewRevision.equals("true")) {

            String orgFileNm = request.getParameter("orgFileNm");
            String prevObjectId = request.getParameter("objectId");
            String currentPageNo = "1";
            String statudCd = "Release";

            // obejctId로 조회 DB에서 조회
            DrawingRequestVO prevRevisionData = dao.getPrevRevisionData(prevObjectId);

            // 새로운 objectId 생성
            String filePathNm = buildDrawingSavedPath(
                    objectId, FilenameUtils.getExtension(request.getParameter("orgFileNm")));

            // 업데이트된 리비전 (기존 리비전 + 1)
            String updatedRevNo = String.valueOf(Integer.parseInt(prevRevisionData.getRevNo()) + 1);

            drawingRegisterPopupParam = DrawingRegisterPopupParam.builder()
                    .drawingNo(request.getParameter("prevDrawingNo"))
                    .orgFileNm(orgFileNm)
                    .objectType(request.getParameter("objectType"))
                    .totalPageNo(prevRevisionData.getTotalPageNo().toString())
                    .currentPageNo(currentPageNo)
                    .businessTypeCd(prevRevisionData.getBusinessTypeCd())
                    .distributeTypeCd(prevRevisionData.getDistributeTypeCd())
                    .objectId(objectId)
                    .fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
                    .fileNm(request.getParameter("fileName"))
                    .revNo(updatedRevNo)
                    .statusCd(statudCd)
                    .drawingType(drawingType)
                    .protectYn(prevRevisionData.getProtectYn())
                    .filePath(filePathNm)
                    .distributionPoint(distributionPoint)
                    .modelCode(prevRevisionData.getModelCode())
//					.customerRevision(request.getParameter("customerRevision"))
                    .build();
        }
        else{
            String currentPageNo = "1";
            String statudCd = "Release";
            String revNo = "0";

            String filePathNm = buildDrawingSavedPath(
                    objectId, FilenameUtils.getExtension(request.getParameter("orgFileNm")));

            // DrawingRegisterPopupParam 객체 만들어서 저장
            drawingRegisterPopupParam = DrawingRegisterPopupParam.builder()
                    .orgFileNm(request.getParameter("orgFileNm"))
                    .objectType(request.getParameter("objectType"))
                    .totalPageNo(request.getParameter("totalPageNo"))
                    .currentPageNo(currentPageNo)
                    .businessTypeCd(request.getParameter("businessTypeCd"))
                    .distributeTypeCd(request.getParameter("distributeTypeCd"))
                    .objectId(objectId)
                    .fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
                    .fileNm(request.getParameter("fileName"))
                    .revNo(revNo)
                    .statusCd(statudCd)
                    .drawingType(drawingType)
                    .protectYn(request.getParameter("protectYn"))
                    .filePath(filePathNm)
                    .distributionPoint(distributionPoint)
                    .modelCode(request.getParameter("modelCode"))
//					.customerRevision(request.getParameter("customerRevision"))
                    .build();
        }
        String existingFileName = dao.getDrawingRegisterByOrgFileNm(objectId);
        if(existingFileName != null){
            resultVo.setSuccess(false);
            resultVo.setMessage("이미 있는 파일입니다");
            return resultVo;

        }else {
            if (isNewRevision != null && isNewRevision.equals("true")) {
                // 도면 리비전 업데이트 시
                dao.insertDrawingRevisionUpdate(drawingRegisterPopupParam);
            } else {
                // 도면 등록 시
                dao.insertDrawingRegisterInfo(drawingRegisterPopupParam);
            }

            drawingRegisterPopupParam = getFileSavedPath2D(drawingRegisterPopupParam, file);

            resultVo.setSuccess(true);
            return resultVo;
        }
    }


    private DrawingRegisterPopupParam getFileSavedPath2D(DrawingRegisterPopupParam param, MultipartFile mf){
//		String filePathNm = SystemConfig.getSystemConfigValue("2D_FILE_PATH")+"\\" + param.getObjectId() +"." + FilenameUtils.getExtension(param.getOrgFileNm());
        String filePathNm = param.getFilePath();

        String orgName = mf.getOriginalFilename();

        if(orgName.contains(File.separator)) {
            orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
        }
        orgName = StringUtil.replaceLfiPath(orgName);
        String path = filePathNm;
        path = StringUtil.replaceLfiPath(path);
        File file = new File(path);
        param.setFilePath(path);

        param.setFileNm(orgName);
        param.setFileSize(String.valueOf(mf.getSize()));

        try {
            file.createNewFile();

            // 0703 (yskim)
            byte[] bytes = mf.getBytes();
            Files.write(Paths.get(path), bytes);
            //

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return param;
    }

    public String compareImageRequest(RequestParam param, Authentication authentication) throws UnsupportedEncodingException {
        List<Map<String,Object>> dbConfig = pdfDao.selectDbConfig();
        String baseUrl="";
        String imgCompareUrl="";

        for(Map<String,Object> config : dbConfig) {
            if(config.get("SYSTEM_CONFIG_CD").equals("BASE_URL")){
//				baseUrl = config.get("SYSTEM_CONFIG_VALUE").toString();
                baseUrl = "http://localhost:3508";
            }
            if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_IMG_COMPARE_URL")){
//				imgCompareUrl = config.get("SYSTEM_CONFIG_VALUE").toString();
                imgCompareUrl = "http://localhost:7442/cv_diff";
            }
        }

        String prevObjectId = param.getPrevObjectId();
        String curObjectId = param.getCurObjectId();
        String objectType = param.getObjectType();

        UserVO userVo = (UserVO) authentication.getPrincipal();
        String userId = userVo.getUserCd();

        String encodedPrevFilePath = URLEncoder.encode(param.getPrevFilePath(), "UTF-8");
        String encodedCurFilePath = URLEncoder.encode(param.getCurFilePath(), "UTF-8");

//		String urlPrev = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
//				+ "?file=" + prevObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + encodedPrevFilePath;
        String urlPrev = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
                + "?file=" + prevObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + param.getPrevFilePath();
//		String urlCur = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
//				+ "?file=" + curObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + encodedCurFilePath;
        String urlCur = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
                + "?file=" + curObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + param.getCurFilePath();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 두 GET 요청 실행
            ResponseEntity<Map> responsePrev = restTemplate.getForEntity(urlPrev, Map.class);
            ResponseEntity<Map> responseCur = restTemplate.getForEntity(urlCur, Map.class);

            Map<String, Object> bodyPrev = responsePrev.getBody();
            Map<String, Object> bodyCur = responseCur.getBody();

            // 두 pdfConvert 요청 모두 "success"를 반환한 경우
            if ("success".equals(bodyPrev.get("status")) && "success".equals(bodyCur.get("status"))) {
                String prevFilePath = (String)bodyPrev.get("cvrtFilePathNm");
                String curFilePath = (String)bodyCur.get("cvrtFilePathNm");
                String prevRevNo = param.getPrevRevNo();
                String curRevNo = param.getCurRevNo();

                // 이미지 비교 리다이렉트 URL 구성
                String redirectUrl = imgCompareUrl
                        + "?Prev_Path=" + prevFilePath
                        + "&Cur_Path=" + curFilePath
                        + "&Prev_DisplayLabel=" + prevRevNo
                        + "&Cur_DisplayLabel=" + curRevNo
                        + "&Request_Flag=kt1b_dms";

                return "redirect:" + redirectUrl;
            } else {
                // 두 요청 중 하나 또는 모두가 "success"가 아닌 경우
                if (!"success".equals(bodyPrev) && !"success".equals(bodyCur)) {
                    // 양쪽 모두 변환 실패한 경우
                    return "both_failed";
                } else if (!"success".equals(bodyPrev)) {
                    // 낮은 리비전 파일 변환 실패한 경우
                    return "prev_failed";
                } else {
                    // 높은 리비전 파일 변환 실패한 경우
                    return "cur_failed";
                }
            }
        } catch (Exception e) {
            log.warn("Drawing PDF comparison failed. cause={}", e.getClass().getSimpleName());
            return "http_exception";
        }
    }

    private String buildDrawingSavedPath(String objectId, String extension) {
        String fileName = objectId
                + (extension == null || extension.trim().isEmpty() ? "" : "." + extension);
        return StoragePathUtils.resolve(
                SystemConfig.getSystemConfigValue("2D_FILE_PATH"), fileName).toString();
    }
}


