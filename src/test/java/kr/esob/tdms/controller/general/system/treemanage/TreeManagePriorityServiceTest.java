package kr.esob.tdms.controller.general.system.treemanage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;

class TreeManagePriorityServiceTest {
	@Test
	void levelListsUseOnlyTheCanonicalTechnicalTreeQueries() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		TreeManageService service = service(dao, prop);
		TreeManageListParam param = new TreeManageListParam();
		param.setManageType("LEVEL");
		param.setParentTreeCd("TRB000001");
		assertEquals("LEVEL", param.getManageType());

		service.selectFunctionCode1List(param);
		service.selectFunctionCode2List(param);
		service.selectDocumentTypeList(param);

		verify(dao).selectBoardFunctionCode1List();
		verify(dao).selectBoardFunctionCode2List(param);
		verify(dao).selectBoardDocumentTypeList(param);
		verifyNoMoreInteractions(dao);
	}

	@Test
	void levelPriorityMustBePositive() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		when(prop.msg("feature.treeManage.validation.priorityPositive"))
			.thenReturn("Priority must be positive.");
		TreeManageService service = service(dao, prop);

		TreeManageSaveParam param = levelParam(0);
		ResultVO result = service.updateNode(param);

		assertFalse(result.isSuccess());
		assertEquals("Priority must be positive.", result.getFailReason());
		verifyNoInteractions(dao);
	}

	@Test
	void levelUpdateUsesOnlyTheCanonicalTechnicalTree() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		when(dao.updateBoardSwNode(org.mockito.ArgumentMatchers.any())).thenReturn(1);
		TreeManageService service = service(dao, prop);
		TreeManageSaveParam param = levelParam(7);

		ResultVO result = service.updateNode(param);

		assertTrue(result.isSuccess());
		assertEquals(7, param.getSortOrder());
		verify(dao).updateBoardSwNode(param);
		verifyNoMoreInteractions(dao);
	}

	@Test
	void levelInsertGeneratesCodeAndWritesOnlyTheCanonicalTechnicalTree() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		when(dao.selectNextBoardTreeCd()).thenReturn("TRB000001");
		when(dao.countBoardSwByTreeCd("TRB000001")).thenReturn(0);
		when(dao.insertBoardSwNode(org.mockito.ArgumentMatchers.any())).thenReturn(1);
		TreeManageService service = service(dao, prop);
		TreeManageSaveParam param = levelParam(3);
		param.setTreeCd(null);

		ResultVO result = service.insertNode(param);

		assertTrue(result.isSuccess());
		assertEquals("TRB000001", param.getTreeCd());
		assertEquals("TRB000001", param.getFunctionCd());
		verify(dao).selectNextBoardTreeCd();
		verify(dao).countBoardSwByTreeCd("TRB000001");
		verify(dao).insertBoardSwNode(param);
		verifyNoMoreInteractions(dao);
	}

	@Test
	void levelDeleteChecksAndDeletesOnlyTheCanonicalTechnicalTree() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		when(dao.countBoardSwChildren("TRB000001")).thenReturn(0);
		when(dao.countLinkedSw("TRB000001")).thenReturn(0);
		when(dao.deleteBoardSwNode("TRB000001")).thenReturn(1);
		TreeManageService service = service(dao, prop);
		TreeManageDeleteParam param = new TreeManageDeleteParam();
		param.setManageType("LEVEL");
		param.setTreeCd("trb000001");

		ResultVO result = service.deleteNode(param);

		assertTrue(result.isSuccess());
		verify(dao).countBoardSwChildren("TRB000001");
		verify(dao).countLinkedSw("TRB000001");
		verify(dao).deleteBoardSwNode("TRB000001");
		verifyNoMoreInteractions(dao);
	}

	private TreeManageSaveParam levelParam(int priority) {
		TreeManageSaveParam param = new TreeManageSaveParam();
		param.setManageType("LEVEL");
		param.setTreeCd("TRB000001");
		param.setTreeNm("Drawing");
		param.setSortOrder(priority);
		return param;
	}

	private TreeManageService service(TreeManageDao dao, Prop prop) {
		TreeManageService service = new TreeManageService();
		ReflectionTestUtils.setField(service, "dao", dao);
		ReflectionTestUtils.setField(service, "prop", prop);
		return service;
	}
}
