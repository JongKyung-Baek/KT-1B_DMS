package kr.esob.fdms.controller.inside.distribution.video;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.distribution.video.VideoInfoVO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public class VideoService implements CommonService{




    @Inject
    VideoDao dao;


    @Override
    public List selectList(Object param) {
        List<VideoRequestVO> rtnList = dao.selectList(param);
        return rtnList;
    }
    @Override
    public int selectListCount(Object param) {
        return dao.selectListCount(param);
    }

    public void setSearchAllParam(VideoRequestParam param) {
        if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
            Gson gson = new Gson();
            param.setVideoList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<VideoInfoVO>>() {}.getType()));
        }
    }


    public String getVideoPath(String objectId){
        return dao.getVideoPath(objectId);
    }

    // 2020.07.24 기범추가( 등록 )
    public ResultVO saveVideoRegisterFileX2(MultipartHttpServletRequest request) throws Exception {
        ResultVO resultVo = new ResultVO();
        System.out.println("들어오는지 체크 = " + request.getParameterMap());
        System.out.println("들어오는지 체크 = " + request.getParameter("fileName"));
        System.out.println("objectType -> "+request.getParameter("objectType"));
        MultipartFile file = request.getFile("file");

//		System.out.println("protectYn 잘 들어오나 " + request.getParameter("protectYn"));

        String objectId = RandomStringGenerator.generateRandomString(32);

        String currentPageNo = "1";
        String statudCd = "Release";
        String revNo = "0";
        String fileNo = "1"; // 파일순서. DOCUMENT_FILE에만 있는거
        String swVersion = "1";
        String docVersion = "1";

        String filePathNm = SystemConfig.getSystemConfigValue("VIDEO_PATH")+"\\"+ objectId+"." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));

        // DrawingRegisterPopupParam 객체 만들어서 저장
        VideoRegisterPopupParam videoRegisterPopupParam = VideoRegisterPopupParam.builder()

                .fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
                .fileNo(fileNo)
                .filePath(filePathNm)
                .docVersion(docVersion)
                .orgFileNm(request.getParameter("orgFileNm"))
                .businessTypeCd(request.getParameter("businessTypeCd"))
                .distributeTypeCd(request.getParameter("distributeTypeCd"))
                .objectId(objectId)
                .fileNm(request.getParameter("fileName"))
                .revNo(revNo)
                .protectYn(request.getParameter("protectYn"))
                .build();

        String existingFileName = dao.getVideoRegisterByOrgFileNm(objectId);
        if (existingFileName != null) {
            resultVo.setSuccess(false);
            resultVo.setMessage("이미 있는 파일입니다");
            return resultVo;
        } else {
            dao.insertVideoRegisterInfo(videoRegisterPopupParam);

// 이 아래 3줄이 있어서 등록 성공이 뜨는거였음. 왜냐 ? getFileSavedPath로 가기 때문
            videoRegisterPopupParam = getFileSavedPath(videoRegisterPopupParam, file);
            resultVo.setSuccess(true);
            return resultVo;
        }
    }

    private VideoRegisterPopupParam getFileSavedPath(VideoRegisterPopupParam param, MultipartFile mf){

        String filePathNm = param.getFilePath();

        File filePath = new File(filePathNm);
        param.setFilePath(filePathNm);
        String orgName = mf.getOriginalFilename();

        if(orgName.contains(File.separator)) {
            orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
        }
        orgName = StringUtil.replaceLfiPath(orgName);
        String path = filePathNm;
        path = StringUtil.replaceLfiPath(path);
        File file = new File(path);
        param.setFilePath(path);
        System.out.println("파일 경로 : " + path);

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

}
