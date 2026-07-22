package kr.esob.fdms.controller.inside.distribution.video;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestParam;
import kr.esob.fdms.controller.inside.distribution.production.ProductionRegisterPopupParam;
import kr.esob.fdms.controller.inside.distribution.production.ProductionRequestParam;
import kr.esob.fdms.controller.inside.distribution.requeststatus.RequestStatusPopupParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.form.FormInfoService;
import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoService;
import kr.esob.fdms.commonlogic.value.SearchAllPopupInfo;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.springframework.web.multipart.MultipartHttpServletRequest;


@Slf4j
@Controller
@RequestMapping("/inside/distribution/videoRequest")
public class VideoController extends AbstractController{

    @Inject
    FormInfoService formService;

    @Inject
    ToolbarInfoService toolbarService;

    @Inject
    GridInfoService gridService;

    @Inject
    ComboService comboService;

    @Inject
    VideoService service;


    @RequestMapping(value="/videoPlay")
    public String videoPopup() throws JsonProcessingException {
        return "inside/distribution/video/videoPlay";
    }

    @RequestMapping(value="/videoTest")
    public String videoPopup1() throws JsonProcessingException {
        return "inside/distribution/video/videoTest";
    }

    @RequestMapping(value="/videoPlayController")
    public String videoPlay(@RequestParam("objectType") String objectType,
                                @RequestParam("videoType") String videoType,
                            @RequestParam("objectId") String objectId, Model model) {

        // 여기서 필요한 로직을 처리합니다. 예를 들어, 영상 파일 경로를 결정합니다.
        System.out.println("파일경로 ~ ~ " + service.getVideoPath(objectId));
        String filePath = service.getVideoPath(objectId);
        String webPath = "/resources/static/videos/" + new File(filePath).getName();
        System.out.println("webPath > > >>  > > " + webPath);

        model.addAttribute("filePath", webPath);

        return "inside/distribution/video/videoPlay"; // 영상 재생 JSP 페이지
    }


    @RequestMapping(value="/")
    public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
//        setHomeParam(model, param);

        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formVideoRequest")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarVideoRequest")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridVideoRequestList")));
        model.addAttribute("viewerUrl", SystemConfig.getSystemConfigValue("VIEWER_URL"));

        return "/inside/distribution/video/videoRequestList";
    }

    @RequestMapping("/selectList")
    public @ResponseBody GridResultVO selectList(VideoRequestParam param) throws Exception {
        service.setSearchAllParam(param);
        GridResultVO result = commonSelectList(param, service);
        return result;
    }

    //2023.07.24 기범추가 ( 등록 )
    @RequestMapping("/videoRegisterPopup")
    public String registerPopup(ProductionRegisterPopupParam param, Model model) {

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = now.format(formatter);

        model.addAttribute("registerUser", param.getSessionUser().getUsername());
        model.addAttribute("date", date);

        return "inside/distribution/video/videoRegisterPopup";
    }


    @PostMapping(value="/uploadVideoRegisFile")
    public @ResponseBody ResultVO uploadVideoRegisFile(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {

        return service.saveVideoRegisterFileX2(multipartHttpServletRequest);
    }

}
