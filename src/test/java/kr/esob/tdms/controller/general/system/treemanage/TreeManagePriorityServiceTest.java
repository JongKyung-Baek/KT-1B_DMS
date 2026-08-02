package kr.esob.tdms.controller.general.system.treemanage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;

class TreeManagePriorityServiceTest {

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
	void levelUpdateSynchronizesTheSamePriorityAcrossAllTechnicalTrees() {
		TreeManageDao dao = mock(TreeManageDao.class);
		Prop prop = mock(Prop.class);
		when(dao.updateBoardSwNode(org.mockito.ArgumentMatchers.any())).thenReturn(1);
		when(dao.updateBoardProductNode(org.mockito.ArgumentMatchers.any())).thenReturn(1);
		when(dao.updateBoardDxfNode(org.mockito.ArgumentMatchers.any())).thenReturn(1);
		TreeManageService service = service(dao, prop);
		TreeManageSaveParam param = levelParam(7);

		ResultVO result = service.updateNode(param);

		assertTrue(result.isSuccess());
		assertEquals(7, param.getSortOrder());
		verify(dao).updateBoardSwNode(param);
		verify(dao).updateBoardProductNode(param);
		verify(dao).updateBoardDxfNode(param);
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
