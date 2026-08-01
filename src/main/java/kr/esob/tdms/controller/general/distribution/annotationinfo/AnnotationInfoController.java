package kr.esob.tdms.controller.general.distribution.annotationinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.controller.general.distribution.commonrequest.CommonDistributionRequestParam;
import net.sf.json.JSONArray;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;



@RequestMapping("/general/distribution/annotationinfo")
@Controller
public class AnnotationInfoController extends AbstractController {

    @Inject
    AnnotationInfoService service;

    @Inject
    ComboService comboService;




    //	  2023.07.04  천기범 추가
    //    2023.07.17  Controller에 주석 넣는 로직 추가
    @RequestMapping(value="/annotationPopup")
    public String annotationPopup(AnnotationRequestParam param, Model model) throws JsonProcessingException {
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAnnotationPopupList")));
        model.addAttribute("objectId", param.getObjectId());
        model.addAttribute("objectType", param.getObjectType());
        return "/general/common/annotationPopup";
    }



    @RequestMapping("/selectAnnotationPopupList")
    public @ResponseBody GridResultVO selectAnnotationPopupList(AnnotationInfoPopupParam param) throws Exception {
        List<AnnotationInfoListVO> originalList = service.selectAnnotationPopupList(param); // timestamp , data, username

        ObjectMapper objectMapper = new ObjectMapper();

        // process 시작
        List<ProcessedAnnotationInfoListVO> processedList = new ArrayList<>();
        for (AnnotationInfoListVO originalItem : originalList) {
            // Json String을 JsonNode로 변환
            JsonNode jsonArray = objectMapper.readTree(originalItem.getData());

            for (JsonNode jsonNode : jsonArray) {
                //필요한 데이터 추출
                String data = jsonNode.get("content").asText();
                String fontsize = jsonNode.get("fontSize").asText();
                String color = jsonNode.get("color").asText();
                String pageNumber = jsonNode.get("pageNumber").asText();
                JsonNode xyPercentage = jsonNode.get("xy_percentage");

                // fontSize 에서 숫자만 추출
                fontsize = fontsize.replaceAll("\\D+","") +"px";

                // xy_percentage를 List로 변환
                List<String> xyList = new ArrayList<>();
                if (xyPercentage.isArray()){
                    for(final JsonNode objNode : xyPercentage){
                        xyList.add(objNode.asText());
                    }
                }

                // 새 VO 객체에 데이터 설정
                ProcessedAnnotationInfoListVO processedItem = new ProcessedAnnotationInfoListVO();
                processedItem.setData(data);
                processedItem.setFontsize(fontsize);
                processedItem.setColor(color);
                processedItem.setPageNumber(pageNumber);
                processedItem.setXyPercentage(xyList.toString());

                // json형식 아닌 일반 노말 데이터
                processedItem.setTimestamp(originalItem.getTimestamp());
                processedItem.setUsername(originalItem.getUserNm());

                processedList.add(processedItem);
            }
        }

        GridResultVO result = new GridResultVO();
        result.setContents(processedList);
        return result;
    }
















//    @RequestMapping(value="/annotationInfoPopup")
//    public String annotationInfoPopup(AnnotationInfoPopupParam param, Model model) throws JsonProcessingException {
//
//
//        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAnnotationInfoPopup")));
////		model.addAttribute("listCount", service.selectPopupListCount(param));
//        model.addAttribute("data", service.getApprovalAnnotationInfo(param));
//        model.addAttribute("objectType", param.getObjectType());
//        return "general/distribution/commonRequest/AnnotationInfoPopup";
//
//    }
//
//
//    @RequestMapping("/annotationInfoPopupList")
//    public @ResponseBody GridResultVO annotationInfoPopupList(AnnotationInfoPopupParam param) throws Exception {
//        GridResultVO result = new GridResultVO();
//        result.setContents(service.annotationInfoPopupList(param));
//        BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
//        BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
//        return result;
//    }


}
