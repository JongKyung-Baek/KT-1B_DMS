package kr.esob.tdms.controller.general.system.roleassign;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;

class RoleAssignServiceTest {
	private RoleAssignDao dao;
	private RoleAssignService service;

	@BeforeEach
	void setUp() {
		dao = mock(RoleAssignDao.class);
		Prop prop = mock(Prop.class);
		when(prop.msg("msg.notSelectedRoleGroup"))
				.thenReturn("Select a user grade.");
		when(prop.msg("msg.noSelectData"))
				.thenReturn("No permission data.");
		when(prop.msg("msg.menuPermissionChanged"))
				.thenReturn("Menu configuration changed.");

		service = new RoleAssignService();
		service.dao = dao;
		service.prop = prop;
	}

	@Test
	void missingGroupCannotBeOverwrittenBySuccess() {
		RequestParam request = new RequestParam();
		request.setList(Collections.singletonList(permission("ROLE_MENU_138", "Y")));

		ResultVO result = service.saveAssign(request);

		assertFalse(result.isSuccess());
		verify(dao, never()).insertRelRoleGroup(any(RequestParam.class));
	}

	@Test
	void emptyPermissionListCannotBeSaved() {
		RequestParam request = requestFor("RG_011");
		request.setList(Collections.emptyList());
		when(dao.selectRoleGroupInfo(request)).thenReturn(new RoleGroupVO());

		ResultVO result = service.saveAssign(request);

		assertFalse(result.isSuccess());
		verify(dao, never()).insertRelRoleGroup(any(RequestParam.class));
	}

	@Test
	void selectedAndClearedRolesArePersistedAtomicallyByOneServiceCall() {
		RequestParam request = requestFor("RG_011");
		RequestParam selected = permission("ROLE_MENU_138", "Y");
		RequestParam cleared = permission("ROLE_MENU_160", "N");
		request.setList(Arrays.asList(selected, cleared));

		when(dao.selectRoleGroupInfo(request)).thenReturn(new RoleGroupVO());
		when(dao.selectAssignableRoleCodes()).thenReturn(
				Arrays.asList("ROLE_MENU_138", "ROLE_MENU_160"));

		ResultVO result = service.saveAssign(request);

		assertTrue(result.isSuccess());
		verify(dao).deleteMenuRoleAssignments("RG_011");
		verify(dao).insertRelRoleGroup(selected);
		verify(dao, never()).insertRelRoleGroup(cleared);
	}

	private RequestParam requestFor(String groupCd) {
		RequestParam request = new RequestParam();
		request.setGroupCd(groupCd);
		return request;
	}

	private RequestParam permission(String roleCd, String selectedYn) {
		RequestParam permission = new RequestParam();
		permission.setRoleCd(roleCd);
		permission.setSelectedYn(selectedYn);
		return permission;
	}
}
