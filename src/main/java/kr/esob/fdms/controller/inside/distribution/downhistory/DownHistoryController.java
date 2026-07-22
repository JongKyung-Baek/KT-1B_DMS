package kr.esob.fdms.controller.inside.distribution.downhistory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.AnnotationRequestParam;
import kr.esob.fdms.controller.inside.distribution.annotationinfo.ProcessedAnnotationInfoListVO;
import kr.esob.fdms.controller.inside.distribution.printhistory.HistoryListParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/downHistory")
public class DownHistoryController extends AbstractController{

    @Inject
    HistoryService service;

    @RequestMapping(value="/")
    public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
        setHomeParam(model, param);
        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDownHistory")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDownHistory")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDownHistoryList")));

        return "inside/distribution/downHistory/downHistoryList";
    }

    @RequestMapping("/selectList")
    public @ResponseBody GridResultVO selectList(DownListParam param) throws Exception {
        GridResultVO result = commonSelectList(param, service);
        return result;
    }

    //// 다운로드 정보
    @RequestMapping(value="/downHistoryPopup")
    public String downHistoryPopup(DownListParam param, Model model) throws JsonProcessingException {
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDownHistoryListPopup")));
        model.addAttribute("objectId", param.getObjectId());
        model.addAttribute("requestNo", param.getRequestNo());

        System.out.println("param.getObjectId() ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ " + param.getObjectId());
        System.out.println("param.getRequestNo() ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ " + param.getRequestNo());

        return "/inside/common/downHistoryPopup";
    }

    @RequestMapping("/selectDownHistoryPopupList")
    public @ResponseBody GridResultVO selectDownHistoryPopupList(DownListParam param) throws Exception {

        List<HistoryListVO> originalList = service.selectDownHistoryPopupList(param);


        // process 시작
        List<HistoryListVO> processedList = new ArrayList<>();
        for (HistoryListVO originalItem : originalList) {
            // Json String을 JsonNode로 변환

            // 새 VO 객체에 데이터 설정
            HistoryListVO processedItem = new HistoryListVO();

            // json형식 아닌 일반 노말 데이터
            System.out.println("originalItem.getRequestNo() ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ" + originalItem.getRequestNo() );
            processedItem.setRequestNo(originalItem.getRequestNo());
            processedItem.setObjectNm(originalItem.getObjectNm());
            processedItem.setObjectNo(originalItem.getObjectNo());
            processedItem.setDownCount(originalItem.getDownCount());
            processedItem.setDownDate(originalItem.getDownDate());
            processedItem.setUserNm(originalItem.getUserNm());
            processedItem.setDownloadedName(originalItem.getDownloadedName());
            processedItem.setActLog("보기");

            processedList.add(processedItem);

        }

        GridResultVO result = new GridResultVO();
        result.setContents(processedList);
        return result;
    }

    //// 다운로드 파일에 대한 행위 이력( 저장, 열기, 수정 등 )
    @RequestMapping(value="/actLogPopup")
    public String actLogPopup(DownListParam param, Model model) throws JsonProcessingException {
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridActLogListPopup")));
        model.addAttribute("downloadedName", param.getDownloadedName());

        System.out.println("param.getDownloadedName() ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ " + param.getDownloadedName());

        return "/inside/common/actLogPopup";
    }

    @RequestMapping("/selectActLogPopupList")
    public @ResponseBody GridResultVO selectActLogPopupList(DownListParam param) throws Exception {

        List<HistoryListVO> originalList = service.selectActLogPopupList(param);

        // process 시작
        List<HistoryListVO> processedList = new ArrayList<>();
        for (HistoryListVO originalItem : originalList) {
            // Json String을 JsonNode로 변환

            // 새 VO 객체에 데이터 설정
            HistoryListVO processedItem = new HistoryListVO();

            // json형식 아닌 일반 노말 데이터
            System.out.println("originalItem.getDownloadedName() ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ" + originalItem.getDownloadedName() );
            processedItem.setDownloadedName(originalItem.getUuid());
            System.out.println("originalItem.getUuid()ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ "+ originalItem.getUuid());
            processedItem.setActTime(originalItem.getActTime());
            processedItem.setActType(originalItem.getActType());

            processedList.add(processedItem);

        }

        GridResultVO result = new GridResultVO();
        result.setContents(processedList);
        return result;
    }










}
