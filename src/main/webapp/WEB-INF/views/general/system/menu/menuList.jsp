<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:message code="feature.locale.code" text="ko" var="pageLanguage" />
<!doctype html>
<html lang="${pageLanguage}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><spring:message code="form.roleGroup" text="메뉴 권한" /></title>
<link type="text/css" rel="stylesheet"
    href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css"
    media="screen" />
<link type="text/css" rel="stylesheet"
    href="${pageContext.request.contextPath}/resources/css/pages/menu-permission.css?v=20260801.2"
    media="screen" />
<script>
    var menuTreeList = [];
var toolbarInfo = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${toolbarInfo}</spring:escapeBody>';

    $(function () {
        $('.layout-wrapper.bodyWrap .content-wrapper > .container')
            .addClass('distribution-invoice-container');
    });
</script>
<script type="text/javascript"
    src="${pageContext.request.contextPath}/resources/js/views/general/system/menu/menuList-vuexy.js?v=20260801.2"></script>
</head>
<body>
<main class="menu-permission-page" aria-labelledby="menuPermissionTitle">
    <header class="menu-permission-header">
        <div class="menu-permission-heading">
            <p class="menu-permission-eyebrow">
                <i class="icon-base ti tabler-settings" aria-hidden="true"></i>
                <spring:message code="menu.sysmanage" text="시스템 관리" />
            </p>
            <h1 id="menuPermissionTitle">
                <spring:message code="form.roleGroup" text="메뉴 권한" />
            </h1>
        </div>
        <div class="menu-permission-summary" aria-live="polite">
            <span class="menu-permission-chip menu-permission-chip--primary">
                <i class="icon-base ti tabler-sitemap" aria-hidden="true"></i>
                <span><spring:message code="label.allMenus" text="전체 메뉴" /></span>
                <strong id="menuTotalCount">0</strong>
            </span>
            <span class="menu-permission-chip menu-permission-chip--active">
                <i class="icon-base ti tabler-circle-check" aria-hidden="true"></i>
                <span><spring:message code="form.useYn" text="사용여부" /></span>
                <strong id="menuActiveCount">0</strong>
            </span>
        </div>
    </header>

    <section class="menu-permission-workspace">
        <article class="menu-permission-panel menu-permission-tree-panel">
            <header class="menu-permission-panel-header">
                <div>
                    <span class="menu-permission-panel-kicker">TREE</span>
                    <h2><spring:message code="label.allMenus" text="전체 메뉴" /></h2>
                </div>
                <div class="btnArea menu-permission-toolbar" id="menuBtnArea"></div>
            </header>

            <form id="menuTreeSearchForm" class="menu-permission-search" role="search">
                <label class="menu-permission-sr-only" for="menuTreeSearch">
                    <spring:message code="btn.search" text="조회" />
                </label>
                <span class="menu-permission-search-field">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                    <input type="search" id="menuTreeSearch" autocomplete="off"
                        placeholder="<spring:message code='label.allMenus' text='전체 메뉴' />" />
                </span>
                <button type="submit" id="menuTreeSearchButton"
                    class="menu-permission-button menu-permission-button--primary">
                    <spring:message code="btn.search" text="조회" />
                </button>
            </form>

            <div class="menu-permission-tree-shell">
                <div id="menuTree" aria-label="<spring:message code='label.allMenus' text='전체 메뉴' />"></div>
            </div>
        </article>

        <aside class="menu-permission-panel menu-permission-detail-panel"
            aria-labelledby="selectedMenuPanelTitle">
            <header class="menu-permission-panel-header">
                <div>
                    <span class="menu-permission-panel-kicker">DETAIL</span>
                    <h2 id="selectedMenuPanelTitle">
                        <spring:message code="form.menuNm" text="메뉴명" />
                    </h2>
                </div>
            </header>

            <div id="menuSelectionEmpty" class="menu-selection-empty">
                <span class="menu-selection-empty-icon" aria-hidden="true">
                    <i class="icon-base ti tabler-pointer"></i>
                </span>
                <p><spring:message code="msg.plzSelectMenu" text="메뉴를 선택해주세요" /></p>
            </div>

            <div id="menuSelectionDetail" class="menu-selection-detail" hidden>
                <div class="menu-selection-identity">
                    <div class="menu-selection-chips">
                        <span id="selectedMenuType" class="menu-permission-chip">TYPE</span>
                        <span id="selectedMenuState"
                            class="menu-permission-chip menu-permission-chip--active">Y</span>
                    </div>
                    <h3 id="selectedMenuName">-</h3>
                    <code id="selectedMenuCode">-</code>
                </div>

                <dl class="menu-selection-meta">
                    <div>
                        <dt><spring:message code="form.parentMenuNm" text="상위메뉴" /></dt>
                        <dd id="selectedMenuParent">-</dd>
                    </div>
                    <div>
                        <dt>LEVEL</dt>
                        <dd id="selectedMenuLevel">-</dd>
                    </div>
                    <div>
                        <dt><spring:message code="form.roleGroup" text="메뉴 권한" /></dt>
                        <dd><code id="selectedMenuRole">-</code></dd>
                    </div>
                    <div>
                        <dt><spring:message code="form.useYn" text="사용여부" /></dt>
                        <dd id="selectedMenuUseYn">-</dd>
                    </div>
                </dl>
            </div>
        </aside>
    </section>
</main>
</body>
</html>
