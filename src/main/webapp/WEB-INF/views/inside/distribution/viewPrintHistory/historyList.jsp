<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.history.access.browserTitle" text="접근이력" var="pageTitle"/>
<spring:message code="feature.history.access.keywordPlaceholder"
                text="사용자, 자료 ID, 요청번호, 추적 ID" var="keywordPlaceholder"/>
<spring:message code="feature.history.access.tableAria" text="접근이력 목록" var="tableAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - KT-1B DMS</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen">
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/access-history.css?v=20260726.7" media="screen">
<script>
    window.accessHistoryConfig = {
        contextPath: '${pageContext.request.contextPath}'
    };

    $(function () {
        $('.layout-wrapper.bodyWrap .content-wrapper > .container')
            .addClass('distribution-invoice-container');
    });
</script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/viewPrintHistory/access-history.js?v=20260726.4"></script>
</head>
<body>
<main class="distribution-invoice-page access-history-page" aria-labelledby="accessLogTitle">
    <section class="ah-log-card" aria-labelledby="accessLogTitle">
        <div class="ah-log-card__header">
            <div>
                <span class="ah-section-kicker"><spring:message code="feature.history.access.kicker" text="접근 및 권한"/></span>
                <h2 id="accessLogTitle"><spring:message code="feature.history.access.title" text="접근·권한 변경 이력"/></h2>
                <p><spring:message code="feature.history.access.description"
                                   text="서버에 영속 저장된 자료 접근과 권한 변경 기록을 최신순으로 표시합니다."/></p>
            </div>
            <span class="ah-count" id="accessHistoryCount"><spring:message code="feature.common.count" text="{0}건" arguments="0"/></span>
        </div>

        <form id="accessHistorySearchForm" class="ah-search" role="search" autocomplete="off">
            <label>
                <span><spring:message code="feature.history.access.filter.eventType" text="이력 유형"/></span>
                <select id="accessEventType">
                    <option value=""><spring:message code="feature.common.all" text="전체"/></option>
                    <option value="FILE_ACCESS"><spring:message code="feature.history.event.fileAccess" text="자료 접근"/></option>
                    <option value="DOWNLOAD_RESULT"><spring:message code="feature.history.event.download" text="다운로드"/></option>
                    <option value="ACL_CHANGE"><spring:message code="feature.history.event.aclChange" text="권한 변경"/></option>
                </select>
            </label>
            <label class="ah-search__keyword">
                <span><spring:message code="feature.common.search.keyword" text="통합검색"/></span>
                <input type="search" id="accessKeyword" maxlength="100"
                       placeholder="${keywordPlaceholder}">
            </label>
            <div class="ah-search__actions">
                <button type="button" class="ah-button ah-button--ghost" id="accessResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i><spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="ah-button ah-button--primary" id="accessSearchButton">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i><spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div id="accessHistoryMessage" class="ah-message" role="status" aria-live="polite" hidden></div>

        <div class="ah-table-wrap">
            <table class="ah-table" aria-label="${tableAria}">
                <colgroup>
                    <col class="ah-col-time"><col class="ah-col-user"><col class="ah-col-menu">
                    <col class="ah-col-action"><col class="ah-col-resource"><col class="ah-col-reason">
                    <col class="ah-col-ip">
                </colgroup>
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.history.column.time" text="일시"/></th>
                    <th scope="col"><spring:message code="feature.history.column.user" text="사용자"/></th>
                    <th scope="col"><spring:message code="feature.history.column.menu" text="메뉴"/></th>
                    <th scope="col"><spring:message code="feature.history.column.action" text="행위"/></th>
                    <th scope="col"><spring:message code="feature.history.column.resource" text="대상 자료"/></th>
                    <th scope="col"><spring:message code="feature.history.column.reason" text="사유·메시지"/></th>
                    <th scope="col"><spring:message code="feature.history.column.ip" text="IP"/></th>
                </tr>
                </thead>
                <tbody id="accessHistoryTableBody">
                <tr class="ah-loading-row">
                    <td colspan="7"><span class="ah-spinner" aria-hidden="true"></span><spring:message
                            code="feature.history.access.loading" text="접근이력을 불러오는 중입니다."/></td>
                </tr>
                </tbody>
            </table>
        </div>
        <footer class="ah-table-footer">
            <span><i class="icon-base ti tabler-info-circle" aria-hidden="true"></i><spring:message
                    code="feature.history.footer.limit" text="최대 1,000건을 최신순으로 조회합니다."/></span>
            <span><spring:message code="feature.history.access.footer.permission"
                                  text="민감한 접근기록은 메뉴 권한이 있는 사용자만 조회할 수 있습니다."/></span>
        </footer>
    </section>

</main>
</body>
</html>
