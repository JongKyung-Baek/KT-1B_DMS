package kr.esob.fdms.controller.inside.system.treemanage;

import kr.esob.fdms.commonlogic.result.ResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Service
public class TreeManageService {
	@Inject
	private TreeManageDao dao;

	public List<TreeManageNodeVO> selectFunctionCode1List(TreeManageListParam param) {
		if (isLevelType(param == null ? null : param.getManageType())) {
			return dao.selectBoardFunctionCode1List();
		}
		return dao.selectFunctionCode1List();
	}

	public List<TreeManageNodeVO> selectFunctionCode2List(TreeManageListParam param) {
		if (isLevelType(param == null ? null : param.getManageType())) {
			return dao.selectBoardFunctionCode2List(param);
		}
		return dao.selectFunctionCode2List(param);
	}

	public List<TreeManageNodeVO> selectDocumentTypeList(TreeManageListParam param) {
		if (isLevelType(param == null ? null : param.getManageType())) {
			return dao.selectBoardDocumentTypeList(param);
		}
		return dao.selectDocumentTypeList(param);
	}

	@Transactional
	public ResultVO insertNode(TreeManageSaveParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if (param == null || isBlank(param.getTreeNm())) {
			result.setFailReason("명칭을 입력해 주세요.");
			return result;
		}
		param.setTreeNm(param.getTreeNm().trim());
		if (!isValidTreeNm(param.getTreeNm())) {
			result.setFailReason("명칭은 1~128자로 입력해 주세요.");
			return result;
		}

		if (isBlank(param.getParentTreeCd())) {
			param.setParentTreeCd(null);
		} else {
			param.setParentTreeCd(param.getParentTreeCd().trim().toUpperCase());
		}

		// 표시코드(function_cd): 기존 클라이언트 호환을 위해 treeCd가 오면 우선 사용
		if (isBlank(param.getFunctionCd())) {
			param.setFunctionCd(param.getTreeCd());
		}
		if (!isBlank(param.getFunctionCd())) {
			param.setFunctionCd(param.getFunctionCd().trim().toUpperCase());
		}

		if (isLevelType(param.getManageType())) {
			param.setTreeCd(generateUniqueTreeCd(true));
			// Level 명칭(tree_nm)과 별개로 function_cd는 길이 제한(varchar(10))에 맞춰 서버에서 자동 부여
			param.setFunctionCd(param.getTreeCd());
			int swAffected = dao.insertBoardSwNode(param);
			int productAffected = dao.insertBoardProductNode(param);
			int dxfAffected = dao.insertBoardDxfNode(param);
			result.setSuccess(swAffected > 0 && productAffected > 0 && dxfAffected > 0);
			if (!result.isSuccess()) {
				result.setFailReason("등록에 실패했습니다.");
			}
			return result;
		}

		if (isBlank(param.getFunctionCd())) {
			result.setFailReason("코드를 입력해 주세요.");
			return result;
		}
		if (!isValidTreeCd(param.getFunctionCd())) {
			result.setFailReason("코드는 영문 대문자/숫자/_ 만 입력 가능합니다.");
			return result;
		}

		if ("ROOT".equalsIgnoreCase(param.getParentTreeCd()) && param.getFunctionCd().startsWith("FC")) {
			param.setParentTreeCd(null);
		}

		if (!isBlank(param.getParentTreeCd())) {
			String parentTreeCdExact = dao.selectDrawingTreeCdExact(param.getParentTreeCd());
			if (isBlank(parentTreeCdExact)) {
				result.setFailReason("상위 코드를 찾을 수 없습니다.");
				return result;
			}
			param.setParentTreeCd(parentTreeCdExact);
		}

		int parentDepth = resolveDrawingParentDepth(param.getParentTreeCd());
		if (!isBlank(param.getParentTreeCd()) && parentDepth <= 0) {
			result.setFailReason("상위 코드를 찾을 수 없습니다.");
			return result;
		}
		boolean syncToIoc = parentDepth <= 1;

		Integer drawingExists = dao.countByFunctionCd(param.getFunctionCd());
		Integer docExists = syncToIoc ? dao.countIocByFunctionCd(param.getFunctionCd()) : 0;
		if ((drawingExists != null && drawingExists > 0) || (syncToIoc && docExists != null && docExists > 0)) {
			result.setFailReason("이미 사용 중인 코드입니다.");
			return result;
		}

		param.setTreeCd(generateUniqueTreeCd(false));
		int drawingAffected = dao.insertNode(param);
		int docAffected = syncToIoc ? dao.insertIocNode(param) : 1;
		if (drawingAffected > 0 && parentDepth == 1) {
			insertDefaultDocTypesForDrawing(param.getTreeCd(), param.getFunctionCd());
		}
		result.setSuccess(drawingAffected > 0 && docAffected > 0);
		if (!result.isSuccess()) {
			result.setFailReason("등록에 실패했습니다.");
		}
		return result;
	}

	@Transactional
	public ResultVO updateNode(TreeManageSaveParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if (param == null || isBlank(param.getTreeCd()) || isBlank(param.getTreeNm())) {
			result.setFailReason("수정 대상을 선택해 주세요.");
			return result;
		}

		param.setTreeCd(param.getTreeCd().trim().toUpperCase());
		param.setTreeNm(param.getTreeNm().trim());
		if (!isValidTreeNm(param.getTreeNm())) {
			result.setFailReason("명칭은 1~128자로 입력해 주세요.");
			return result;
		}

		if (isLevelType(param.getManageType())) {
			int swAffected = dao.updateBoardSwNode(param);
			int productAffected = dao.updateBoardProductNode(param);
			int dxfAffected = dao.updateBoardDxfNode(param);
			result.setSuccess(swAffected > 0 && productAffected > 0 && dxfAffected > 0);
			if (!result.isSuccess()) {
				result.setFailReason("수정에 실패했습니다.");
			}
			return result;
		}

		int drawingAffected = dao.updateNode(param);
		Integer iocExists = dao.countIocByTreeCd(param.getTreeCd());
		int docAffected = (iocExists != null && iocExists > 0) ? dao.updateIocNode(param) : 1;
		result.setSuccess(drawingAffected > 0 && docAffected > 0);
		if (!result.isSuccess()) {
			result.setFailReason("수정에 실패했습니다.");
		}
		return result;
	}

	@Transactional
	public ResultVO deleteNode(TreeManageDeleteParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if (param == null || isBlank(param.getTreeCd())) {
			result.setFailReason("삭제 대상을 선택해 주세요.");
			return result;
		}

		String treeCd = param.getTreeCd().trim().toUpperCase();

		if (isLevelType(param.getManageType())) {
			Integer swChildCount = dao.countBoardSwChildren(treeCd);
			Integer productChildCount = dao.countBoardProductChildren(treeCd);
			Integer dxfChildCount = dao.countBoardDxfChildren(treeCd);
			if ((swChildCount != null && swChildCount > 0) || (productChildCount != null && productChildCount > 0)
					|| (dxfChildCount != null && dxfChildCount > 0)) {
				result.setFailReason("하위 코드가 있어 삭제할 수 없습니다.");
				return result;
			}

			Integer swLinkedCount = dao.countLinkedSw(treeCd);
			Integer productLinkedCount = dao.countLinkedProduct(treeCd);
			Integer dxfLinkedCount = dao.countLinkedDxf(treeCd);
			if ((swLinkedCount != null && swLinkedCount > 0) || (productLinkedCount != null && productLinkedCount > 0)
					|| (dxfLinkedCount != null && dxfLinkedCount > 0)) {
				result.setFailReason("연결된 데이터가 있어 삭제할 수 없습니다.");
				return result;
			}

			int swAffected = dao.deleteBoardSwNode(treeCd);
			int productAffected = dao.deleteBoardProductNode(treeCd);
			int dxfAffected = dao.deleteBoardDxfNode(treeCd);
			result.setSuccess(swAffected > 0 && productAffected > 0 && dxfAffected > 0);
			if (!result.isSuccess()) {
				result.setFailReason("삭제에 실패했습니다.");
			}
			return result;
		}

		Integer drawingChildCount = dao.countChildren(treeCd);
		Integer docChildCount = dao.countIocChildren(treeCd);
		if ((drawingChildCount != null && drawingChildCount > 0) || (docChildCount != null && docChildCount > 0)) {
			result.setFailReason("하위 코드가 있어 삭제할 수 없습니다.");
			return result;
		}

		Integer drawingLinkedCount = dao.countLinkedDrawing(treeCd);
		Integer docLinkedCount = dao.countLinkedDocument(treeCd);
		if ((drawingLinkedCount != null && drawingLinkedCount > 0) || (docLinkedCount != null && docLinkedCount > 0)) {
			result.setFailReason("연결된 데이터가 있어 삭제할 수 없습니다.");
			return result;
		}

		int drawingAffected = dao.deleteNode(treeCd);
		Integer iocExists = dao.countIocByTreeCd(treeCd);
		int docAffected = (iocExists != null && iocExists > 0) ? dao.deleteIocNode(treeCd) : 1;
		result.setSuccess(drawingAffected > 0 && docAffected > 0);
		if (!result.isSuccess()) {
			result.setFailReason("삭제에 실패했습니다.");
		}
		return result;
	}

	private boolean isLevelType(String manageType) {
		return "LEVEL".equalsIgnoreCase(manageType);
	}

	private boolean isValidTreeCd(String code) {
		return code != null && code.matches("[A-Z0-9_]+$");
	}

	private boolean isValidTreeNm(String name) {
		return name != null && !name.trim().isEmpty() && name.trim().length() <= 128;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private int resolveDrawingParentDepth(String parentTreeCd) {
		if (isBlank(parentTreeCd)) {
			return 0;
		}
		Integer depth = dao.selectDrawingTreeDepth(parentTreeCd.trim().toUpperCase());
		return depth == null ? 0 : depth;
	}

	private void insertDefaultDocTypesForDrawing(String parentTreeCd, String parentFunctionCd) {
		List<String> defaultDocTypes = Arrays.asList("SP", "D0", "D1", "D2");
		for (String docType : defaultDocTypes) {
			String childFunctionCd = parentFunctionCd + "_" + docType;
			Integer exists = dao.countByFunctionCd(childFunctionCd);
			if (exists != null && exists > 0) {
				continue;
			}

			TreeManageSaveParam childParam = new TreeManageSaveParam();
			childParam.setTreeCd(generateUniqueTreeCd(false));
			childParam.setFunctionCd(childFunctionCd);
			childParam.setParentTreeCd(parentTreeCd);
			childParam.setTreeNm(docType);
			childParam.setManageType("DOC");
			int affected = dao.insertNode(childParam);
			if (affected <= 0) {
				throw new IllegalStateException("Document Type 기본코드 생성에 실패했습니다.");
			}
		}
	}

	private String generateUniqueTreeCd(boolean boardType) {
		String candidate = boardType ? dao.selectNextBoardTreeCd() : dao.selectNextDocTreeCd();
		if (isBlank(candidate)) {
			throw new IllegalStateException("TREE_CD 생성 실패");
		}

		String prefix = boardType ? "TRB" : "TRD";
		long seq = parseTreeCdSeq(candidate, prefix);
		for (int i = 0; i < 1000; i++) {
			String current = prefix + String.format("%06d", seq + i);
			if (!existsTreeCd(boardType, current)) {
				return current;
			}
		}
		throw new IllegalStateException("TREE_CD 생성 실패");
	}

	private long parseTreeCdSeq(String treeCd, String prefix) {
		if (isBlank(treeCd) || !treeCd.startsWith(prefix)) {
			return 1L;
		}
		String seq = treeCd.substring(prefix.length());
		try {
			return Long.parseLong(seq);
		} catch (NumberFormatException e) {
			return 1L;
		}
	}

	private boolean existsTreeCd(boolean boardType, String treeCd) {
		if (boardType) {
			Integer sw = dao.countBoardSwByTreeCd(treeCd);
			Integer product = dao.countBoardProductByTreeCd(treeCd);
			Integer dxf = dao.countBoardDxfByTreeCd(treeCd);
			return (sw != null && sw > 0) || (product != null && product > 0) || (dxf != null && dxf > 0);
		}
		Integer drawing = dao.countByTreeCd(treeCd);
		Integer doc = dao.countIocByTreeCd(treeCd);
		return (drawing != null && drawing > 0) || (doc != null && doc > 0);
	}
}
