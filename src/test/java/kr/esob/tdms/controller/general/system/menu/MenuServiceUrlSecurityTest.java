package kr.esob.tdms.controller.general.system.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.toolbar.ToolbarInfoDao;
import kr.esob.tdms.controller.general.system.roleassign.RoleAssignDao;

class MenuServiceUrlSecurityTest {
    @Test
    void rejectsUnsafeInsertAndUpdateBeforeAnyDatabaseAccess() {
        MenuDao dao = mock(MenuDao.class);
        RoleAssignDao roleAssignDao = mock(RoleAssignDao.class);
        ToolbarInfoDao toolbarDao = mock(ToolbarInfoDao.class);
        MenuService service = service(dao, roleAssignDao, toolbarDao);

        for (String saveFlag : new String[] {"I", "U"}) {
            MenuSaveRequestParam param = new MenuSaveRequestParam();
            param.setSaveFlag(saveFlag);
            param.setMenuUrl("javascript:alert(document.domain)");

            ResultVO result = service.saveMenu(param);

            assertFalse(result.isSuccess());
            assertEquals("Menu URL must be an internal absolute path.",
                    result.getFailReason());
        }

        verifyNoInteractions(dao, roleAssignDao, toolbarDao);
    }

    @Test
    void normalInternalUpdateRetainsTheExistingSaveContract() {
        MenuDao dao = mock(MenuDao.class);
        RoleAssignDao roleAssignDao = mock(RoleAssignDao.class);
        ToolbarInfoDao toolbarDao = mock(ToolbarInfoDao.class);
        MenuService service = service(dao, roleAssignDao, toolbarDao);
        MenuVO original = new MenuVO();
        original.setRoleCd("ROLE_MENU_101");

        MenuSaveRequestParam param = new MenuSaveRequestParam();
        param.setSaveFlag("U");
        param.setMenuCd("MENU_101");
        param.setMenuLevel("2");
        param.setPopupYn("N");
        param.setUseYn("Y");
        param.setMenuUrl(" /general/distribution/swRequest/** ");
        when(dao.selectMenuInfo(param)).thenReturn(original);

        ResultVO result = service.saveMenu(param);

        assertTrue(result.isSuccess());
        assertEquals("/general/distribution/swRequest/**", param.getMenuUrl());
        verify(dao).selectMenuInfo(param);
        verify(dao).updateMenu(param);
        verify(roleAssignDao).insertRelRoleGroup("RG_001", "ROLE_MENU_101");
    }

    @Test
    void structuralMenuMayKeepAnEmptyUrl() {
        MenuDao dao = mock(MenuDao.class);
        RoleAssignDao roleAssignDao = mock(RoleAssignDao.class);
        ToolbarInfoDao toolbarDao = mock(ToolbarInfoDao.class);
        MenuService service = service(dao, roleAssignDao, toolbarDao);
        MenuVO original = new MenuVO();
        original.setRoleCd("ROLE_MENU_102");

        MenuSaveRequestParam param = new MenuSaveRequestParam();
        param.setSaveFlag("U");
        param.setMenuCd("MENU_102");
        param.setMenuLevel("1");
        param.setPopupYn("N");
        param.setUseYn("Y");
        param.setMenuUrl("  ");
        when(dao.selectMenuInfo(param)).thenReturn(original);

        ResultVO result = service.saveMenu(param);

        assertTrue(result.isSuccess());
        assertEquals("", param.getMenuUrl());
        verify(dao).updateMenu(param);
    }

    private MenuService service(MenuDao dao, RoleAssignDao roleAssignDao,
            ToolbarInfoDao toolbarDao) {
        MenuService service = new MenuService();
        ReflectionTestUtils.setField(service, "dao", dao);
        ReflectionTestUtils.setField(service, "roleAssignDao", roleAssignDao);
        ReflectionTestUtils.setField(service, "toolbarDao", toolbarDao);
        return service;
    }
}
