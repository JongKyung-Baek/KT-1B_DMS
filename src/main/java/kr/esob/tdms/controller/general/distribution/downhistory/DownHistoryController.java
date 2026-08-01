package kr.esob.tdms.controller.general.distribution.downhistory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoListVO;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationInfoPopupParam;
import kr.esob.tdms.controller.general.distribution.annotationinfo.AnnotationRequestParam;
import kr.esob.tdms.controller.general.distribution.annotationinfo.ProcessedAnnotationInfoListVO;
import kr.esob.tdms.controller.general.distribution.printhistory.HistoryListParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.authorization.AuthorizationDao;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/distribution/downHistory")
public class DownHistoryController extends AbstractController{

    @Inject
    HistoryService service;

    @RequestMapping(value="/")
    public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
        setHomeParam(model, param);
        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDownHistory")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDownHistory")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDownHistoryList")));

        return "general/distribution/downHistory/downHistoryList";
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

        return "/general/common/downHistoryPopup";
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

        return "/general/common/actLogPopup";
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
            processedItem.setDownloadedName(originalItem.getUuid());
            processedItem.setActTime(originalItem.getActTime());
            processedItem.setActType(originalItem.getActType());

            processedList.add(processedItem);

        }

        GridResultVO result = new GridResultVO();
        result.setContents(processedList);
        return result;
    }










}
