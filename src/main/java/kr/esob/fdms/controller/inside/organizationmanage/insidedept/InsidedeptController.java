package kr.esob.fdms.controller.inside.organizationmanage.insidedept;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;

@RequestMapping("/inside/organizationmanage/insidedept")
@Controller
public class InsidedeptController extends AbstractController {

    @Inject
    InsidedeptService service;

    @Autowired
    ComboService comboService;

    @Inject
    ComboDao comboDao;

    @RequestMapping(value="/")
    public String home(Model model) throws JsonProcessingException {
        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formInsideDept")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarInsideDept")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridInsideDeptList")));

        return "inside/organizationmanage/insidedept/insidedeptList";
    }

    @RequestMapping("/selectList")
    public @ResponseBody GridResultVO selectList(InsidedeptListParam param) throws Exception {
        GridResultVO result = commonSelectList(param, service);
        return result;
    }

    @RequestMapping(value= {"/registerDeptPopup", "/editDeptPopup"})
    public String registerDept(Model model ,DeptPopupParam param) throws JsonProcessingException {

        DeptListVO info = null;
        String saveFlag = null;

        if(null == param.getDeptCd()) {
            info = new DeptListVO();
            saveFlag = "I"; // 새로운 데이터  (생성)
        }
        else {
            info = service.selectDept(param.getDeptCd());
            saveFlag = "U"; // 이미 있는 데이터 (수정)
        }

        model.addAttribute("info", info);
        model.addAttribute("saveFlag", saveFlag);

        return "inside/organizationmanage/insidedept/registerDeptPopup";
    }

    @PostMapping(value="/saveRegisterDept")
    public @ResponseBody ResultVO saveRegisterDept(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
        return service.saveRegsiterDept(multipartHttpServletRequest);
    }

}
