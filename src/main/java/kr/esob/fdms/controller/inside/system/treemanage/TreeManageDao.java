package kr.esob.fdms.controller.inside.system.treemanage;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class TreeManageDao extends AbstractDao {
	private final String prefix = "sql.SystemTreeManage.";

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectFunctionCode1List() {
		return list(prefix + "selectFunctionCode1List");
	}

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectFunctionCode2List(TreeManageListParam param) {
		return list(prefix + "selectFunctionCode2List", param);
	}

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectDocumentTypeList(TreeManageListParam param) {
		return list(prefix + "selectDocumentTypeList", param);
	}

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectBoardFunctionCode1List() {
		return list(prefix + "selectBoardFunctionCode1List");
	}

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectBoardFunctionCode2List(TreeManageListParam param) {
		return list(prefix + "selectBoardFunctionCode2List", param);
	}

	@SuppressWarnings("unchecked")
	public List<TreeManageNodeVO> selectBoardDocumentTypeList(TreeManageListParam param) {
		return list(prefix + "selectBoardDocumentTypeList", param);
	}

	public int insertNode(TreeManageSaveParam param) {
		return update(prefix + "insertNode", param);
	}

	public int insertIocNode(TreeManageSaveParam param) {
		return update(prefix + "insertIocNode", param);
	}

	public int insertBoardSwNode(TreeManageSaveParam param) {
		return update(prefix + "insertBoardSwNode", param);
	}

	public int insertBoardProductNode(TreeManageSaveParam param) {
		return update(prefix + "insertBoardProductNode", param);
	}

	public int insertBoardDxfNode(TreeManageSaveParam param) {
		return update(prefix + "insertBoardDxfNode", param);
	}

	public int updateNode(TreeManageSaveParam param) {
		return update(prefix + "updateNode", param);
	}

	public int updateIocNode(TreeManageSaveParam param) {
		return update(prefix + "updateIocNode", param);
	}

	public int updateBoardSwNode(TreeManageSaveParam param) {
		return update(prefix + "updateBoardSwNode", param);
	}

	public int updateBoardProductNode(TreeManageSaveParam param) {
		return update(prefix + "updateBoardProductNode", param);
	}

	public int updateBoardDxfNode(TreeManageSaveParam param) {
		return update(prefix + "updateBoardDxfNode", param);
	}

	public Integer countChildren(String treeCd) {
		return (Integer) obj(prefix + "countChildren", treeCd);
	}

	public Integer countIocChildren(String treeCd) {
		return (Integer) obj(prefix + "countIocChildren", treeCd);
	}

	public Integer countBoardSwChildren(String treeCd) {
		return (Integer) obj(prefix + "countBoardSwChildren", treeCd);
	}

	public Integer countBoardProductChildren(String treeCd) {
		return (Integer) obj(prefix + "countBoardProductChildren", treeCd);
	}

	public Integer countBoardDxfChildren(String treeCd) {
		return (Integer) obj(prefix + "countBoardDxfChildren", treeCd);
	}

	public Integer countLinkedDrawing(String treeCd) {
		return (Integer) obj(prefix + "countLinkedDrawing", treeCd);
	}

	public Integer countLinkedDocument(String treeCd) {
		return (Integer) obj(prefix + "countLinkedDocument", treeCd);
	}

	public Integer countLinkedSw(String treeCd) {
		return (Integer) obj(prefix + "countLinkedSw", treeCd);
	}

	public Integer countLinkedProduct(String treeCd) {
		return (Integer) obj(prefix + "countLinkedProduct", treeCd);
	}

	public Integer countLinkedDxf(String treeCd) {
		return (Integer) obj(prefix + "countLinkedDxf", treeCd);
	}

	public Integer countByTreeCd(String treeCd) {
		return (Integer) obj(prefix + "countByTreeCd", treeCd);
	}

	public Integer countIocByTreeCd(String treeCd) {
		return (Integer) obj(prefix + "countIocByTreeCd", treeCd);
	}

	public Integer countBoardSwByTreeCd(String treeCd) {
		return (Integer) obj(prefix + "countBoardSwByTreeCd", treeCd);
	}

	public Integer countBoardProductByTreeCd(String treeCd) {
		return (Integer) obj(prefix + "countBoardProductByTreeCd", treeCd);
	}

	public Integer countBoardDxfByTreeCd(String treeCd) {
		return (Integer) obj(prefix + "countBoardDxfByTreeCd", treeCd);
	}

	public Integer countByFunctionCd(String functionCd) {
		return (Integer) obj(prefix + "countByFunctionCd", functionCd);
	}

	public Integer countIocByFunctionCd(String functionCd) {
		return (Integer) obj(prefix + "countIocByFunctionCd", functionCd);
	}

	public Integer countBoardSwByFunctionCd(String functionCd) {
		return (Integer) obj(prefix + "countBoardSwByFunctionCd", functionCd);
	}

	public Integer countBoardProductByFunctionCd(String functionCd) {
		return (Integer) obj(prefix + "countBoardProductByFunctionCd", functionCd);
	}

	public Integer countBoardDxfByFunctionCd(String functionCd) {
		return (Integer) obj(prefix + "countBoardDxfByFunctionCd", functionCd);
	}

	public Integer selectDrawingTreeDepth(String treeCd) {
		return (Integer) obj(prefix + "selectDrawingTreeDepth", treeCd);
	}

	public String selectDrawingTreeCdExact(String treeCd) {
		return (String) obj(prefix + "selectDrawingTreeCdExact", treeCd);
	}

	public String selectNextDocTreeCd() {
		return (String) obj(prefix + "selectNextDocTreeCd");
	}

	public String selectNextBoardTreeCd() {
		return (String) obj(prefix + "selectNextBoardTreeCd");
	}

	public int deleteNode(String treeCd) {
		return update(prefix + "deleteNode", treeCd);
	}

	public int deleteIocNode(String treeCd) {
		return update(prefix + "deleteIocNode", treeCd);
	}

	public int deleteBoardSwNode(String treeCd) {
		return update(prefix + "deleteBoardSwNode", treeCd);
	}

	public int deleteBoardProductNode(String treeCd) {
		return update(prefix + "deleteBoardProductNode", treeCd);
	}

	public int deleteBoardDxfNode(String treeCd) {
		return update(prefix + "deleteBoardDxfNode", treeCd);
	}
}


