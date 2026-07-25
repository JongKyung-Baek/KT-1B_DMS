package kr.esob.fdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;

class AuditMenuResolverTest {

    @Test
    void productionConstructorIsExplicitlyAutowiredWhenTestConstructorAlsoExists()
            throws NoSuchMethodException {
        assertTrue(AuditMenuResolver.class
                .getConstructor(MenuDao.class)
                .isAnnotationPresent(Autowired.class));
    }

    @Test
    void resolvesOnlyActiveMenusByStaticSpecificityThenDepthAndBuildsParentPath() {
        MenuDao dao = mock(MenuDao.class);
        MenuVO root = menu("MENU_013", null, "기술자료관리",
                "/inside/distribution/swRequest/**", 1, "Y", "N");
        MenuVO child = menu("MENU_220", "MENU_013", "조회",
                "/inside/distribution/swRequest/**", 2, "Y", "N");
        MenuVO exact = menu("MENU_221", "MENU_013", "등록",
                "/inside/distribution/swRequest/regist", 2, "Y", "N");
        MenuVO inactive = menu("MENU_900", null, "비활성",
                "/inside/distribution/swRequest/regist/detail", 9, "N", "N");
        MenuVO deleted = menu("MENU_901", null, "삭제",
                "/inside/distribution/swRequest/regist/detail", 10, "Y", "Y");
        when(dao.getMenuList()).thenReturn(Arrays.asList(root, child, exact, inactive, deleted));

        AuditMenuResolver resolver = new AuditMenuResolver(dao);

        AuditMenuContext list = resolver.resolve("/inside/distribution/swRequest/selectList");
        assertEquals("MENU_220", list.getMenuCd());
        assertEquals("기술자료관리 > 조회", list.getMenuNm());

        AuditMenuContext registration =
                resolver.resolve("/inside/distribution/swRequest/regist");
        assertEquals("MENU_221", registration.getMenuCd());
        assertEquals("기술자료관리 > 등록", registration.getMenuNm());

        AuditMenuContext detail =
                resolver.resolve("/inside/distribution/swRequest/regist/detail");
        assertEquals("MENU_220", detail.getMenuCd());
    }

    @Test
    void exactMenuEndingInSlashOwnsItsPageApis() {
        MenuDao dao = mock(MenuDao.class);
        when(dao.getMenuList()).thenReturn(Arrays.asList(
                menu("MENU_222", null, "보안등급/인가 관리",
                        "/inside/system/securityaccess/", 2, "Y", "N")));

        AuditMenuContext resolved =
                new AuditMenuResolver(dao).resolve("/inside/system/securityaccess/grades");

        assertEquals("MENU_222", resolved.getMenuCd());
        assertEquals("/inside/system/securityaccess/", resolved.getMenuUrl());
        assertEquals("MENU_222",
                new AuditMenuResolver(dao)
                        .resolve("/inside/system/securityaccess")
                        .getMenuCd());
    }

    @Test
    void cacheRefreshesOnlyAfterSixtySeconds() {
        MenuDao dao = mock(MenuDao.class);
        when(dao.getMenuList()).thenReturn(Arrays.asList(
                menu("MENU_1", null, "메뉴", "/inside/menu/**", 1, "Y", "N")));
        AtomicLong ticker = new AtomicLong();
        AuditMenuResolver resolver =
                new AuditMenuResolver(dao, Duration.ofSeconds(60), ticker::get);

        resolver.resolve("/inside/menu/a");
        resolver.resolve("/inside/menu/b");
        verify(dao, times(1)).getMenuList();

        ticker.addAndGet(Duration.ofSeconds(61).toNanos());
        resolver.resolve("/inside/menu/c");
        verify(dao, times(2)).getMenuList();
        assertNull(resolver.resolve("/outside/menu"));
    }

    private MenuVO menu(String menuCd, String parentMenuCd, String menuNm,
                        String menuUrl, int menuLevel, String useYn, String delYn) {
        MenuVO menu = new MenuVO();
        menu.setMenuCd(menuCd);
        menu.setParentMenuCd(parentMenuCd);
        menu.setMenuNm(menuNm);
        menu.setMenuUrl(menuUrl);
        menu.setMenuLevel(menuLevel);
        menu.setUseYn(useYn);
        menu.setDelYn(delYn);
        return menu;
    }
}
