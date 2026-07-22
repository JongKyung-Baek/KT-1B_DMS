package kr.esob.fdms.controller.inside.system.treemanage;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Controller
@RequestMapping("/inside/system/treemanage")
public class TreeManageController extends AbstractController {
	@Inject
	private TreeManageService service;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		return "inside/system/treemanage/treeManage";
	}

	@RequestMapping(value = "/function1/list", method = RequestMethod.POST)
	public @ResponseBody List<TreeManageNodeVO> functionCode1List(@RequestBody(required = false) TreeManageListParam param) {
		return service.selectFunctionCode1List(param);
	}

	@RequestMapping(value = "/function2/list", method = RequestMethod.POST)
	public @ResponseBody List<TreeManageNodeVO> functionCode2List(@RequestBody TreeManageListParam param) {
		return service.selectFunctionCode2List(param);
	}

	@RequestMapping(value = "/doctype/list", method = RequestMethod.POST)
	public @ResponseBody List<TreeManageNodeVO> documentTypeList(@RequestBody TreeManageListParam param) {
		return service.selectDocumentTypeList(param);
	}

	@RequestMapping(value = "/node/add", method = RequestMethod.POST)
	public @ResponseBody ResultVO addNode(@RequestBody TreeManageSaveParam param) {
		return service.insertNode(param);
	}

	@RequestMapping(value = "/node/update", method = RequestMethod.POST)
	public @ResponseBody ResultVO updateNode(@RequestBody TreeManageSaveParam param) {
		return service.updateNode(param);
	}

	@RequestMapping(value = "/node/delete", method = RequestMethod.POST)
	public @ResponseBody ResultVO deleteNode(@RequestBody TreeManageDeleteParam param) {
		return service.deleteNode(param);
	}
}
