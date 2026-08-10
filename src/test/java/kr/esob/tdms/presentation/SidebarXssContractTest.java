package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SidebarXssContractTest {

    @Test
    void menuContextIsReadFromEscapedDomAttributes() throws IOException {
        String sidebar = sidebar();

        assertTrue(sidebar.contains("id=\"current-menu-data\" hidden"));
        assertTrue(sidebar.contains("data-menu-title=\"<c:out value='${menuTitle}' />\""));
        assertTrue(sidebar.contains("data-menu-path=\"<c:out value='${menuPath}' />\""));
        assertTrue(sidebar.contains("data-menu-cd=\"<c:out value='${menuCd}' />\""));
        assertTrue(sidebar.contains("data-menu-path-cd=\"<c:out value='${menuPathCd}' />\""));
        assertTrue(sidebar.contains("document.getElementById('current-menu-data')"));
        assertTrue(sidebar.contains("getAttribute('data-menu-title')"));
        assertTrue(sidebar.contains("getAttribute('data-menu-path')"));
        assertTrue(sidebar.contains("getAttribute('data-menu-cd')"));
        assertTrue(sidebar.contains("getAttribute('data-menu-path-cd')"));
        assertFalse(sidebar.contains("var currentMenuNm = '${menuTitle}'"));
        assertFalse(sidebar.contains("var currentMenuPath = '${menuPath}'"));
        assertFalse(sidebar.contains("var currentMenuCd = '${menuCd}'"));
        assertFalse(sidebar.contains("var currentMenuPathCd = '${menuPathCd}'"));
    }

    @Test
    void breadcrumbsAndMenuLabelsUseTextOrEscapedOutput() throws IOException {
        String sidebar = sidebar();

        assertTrue(sidebar.contains("document.createDocumentFragment()"));
        assertTrue(sidebar.contains("document.createElement('span')"));
        assertTrue(sidebar.contains("menuPathItem.textContent = menuPathName"));
        assertTrue(sidebar.contains("$(\".navBox\").empty().append(getMenuPath())"));
        assertFalse(sidebar.contains("$(\".navBox\").html(getMenuPath())"));
        assertFalse(sidebar.contains("result.push('<span>' + this + '</span>')"));
        assertTrue(sidebar.contains("<div><c:out value=\"${menuTop.menuNm}\" /></div>"));
        assertTrue(sidebar.contains("<div><c:out value=\"${menuSub.menuNm}\" /></div>"));
        assertTrue(sidebar.contains("<div><c:out value=\"${menuLeaf.menuNm}\" /></div>"));
        assertTrue(sidebar.contains("title=\"<c:out value='${menuTop.menuNm}' />\""));
        assertTrue(sidebar.contains("id=\"<c:out value='${menuTop.menuCd}' />\""));
        assertTrue(sidebar.contains("id=\"<c:out value='${menuSub.menuCd}' />\""));
        assertTrue(sidebar.contains("id=\"<c:out value='${menuLeaf.menuCd}' />\""));
        assertTrue(sidebar.contains("href=\"<c:out value='${menuSub.menuUrl}' />\""));
        assertTrue(sidebar.contains("href=\"<c:out value='${menuLeaf.menuUrl}' />\""));
        assertTrue(sidebar.contains("href=\"<c:out value='${topMenuUrl}' />\""));
        assertFalse(sidebar.contains("id=\"${menuTop.menuCd"));
        assertFalse(sidebar.contains("id=\"${menuSub.menuCd"));
        assertFalse(sidebar.contains("id=\"${menuLeaf.menuCd"));
        assertFalse(sidebar.contains("href=\"${menuSub.menuUrl"));
        assertFalse(sidebar.contains("href=\"${menuLeaf.menuUrl"));
        assertFalse(sidebar.contains("href=\"${topMenuUrl"));
        assertFalse(sidebar.contains("<div>${menuTop.menuNm}</div>"));
        assertFalse(sidebar.contains("<div>${menuSub.menuNm}</div>"));
        assertFalse(sidebar.contains("<div>${menuLeaf.menuNm}</div>"));
    }

    private String sidebar() throws IOException {
        return Files.readString(Path.of("src", "main", "webapp", "left.jsp"),
                StandardCharsets.UTF_8);
    }
}
