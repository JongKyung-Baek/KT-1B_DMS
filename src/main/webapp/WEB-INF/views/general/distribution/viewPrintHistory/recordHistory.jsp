<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="printMode" value="${historyMode eq 'print'}"/>
<c:set var="historyTitleFallback" value="${printMode ? '출력이력' : '열람이력'}"/>
<c:set var="historyKickerFallback" value="${printMode ? '문서 출력' : '문서 열람'}"/>
<c:set var="historyDescriptionFallback"
       value="${printMode ? '출력 작업 원장과 대상 파일을 기준으로 요청 시각과 출력 정보를 표시합니다.' : '뷰어에서 실제 열람이 완료되어 저장된 기록만 표시합니다.'}"/>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="${historyTitleCode}" text="${historyTitleFallback}" var="historyTitle"/>
<spring:message code="${historyKickerCode}" text="${historyKickerFallback}" var="historyKicker"/>
<spring:message code="${historyDescriptionCode}" text="${historyDescriptionFallback}" var="historyDescription"/>
<c:set var="historyKeywordCode"
       value="${printMode ? 'feature.history.print.keywordPlaceholder' : 'feature.history.view.keywordPlaceholder'}"/>
<spring:message code="${historyKeywordCode}"
                text="${printMode ? '사용자, 작업 ID, 자료 ID, 파일번호, 프린터' : '사용자, 문서번호, 자료 ID, 파일명, 파일번호'}"
                var="historyKeywordPlaceholder"/>
<spring:message code="feature.history.listTitle" text="{0} 목록"
                arguments="${historyTitle}" var="historyListTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${historyTitle}"/> - KT-1B DMS</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen">
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/access-history.css?v=20260731.1" media="screen">
<script>
    window.recordHistoryConfig = {
        contextPath: '${pageContext.request.contextPath}',
        endpoint: '${historyEndpoint}',
        mode: '${historyMode}'
    };

    $(function () {
        $('.layout-wrapper.bodyWrap .content-wrapper > .container')
            .addClass('distribution-invoice-container');
    });
</script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/general/distribution/viewPrintHistory/record-history.js?v=20260731.1"></script>
</head>
<body>
<main class="distribution-invoice-page access-history-page record-history-page record-history-page--${historyMode}"
      aria-labelledby="recordLogTitle">
    <section class="ah-log-card" aria-labelledby="recordLogTitle">
        <div class="ah-log-card__header">
            <div>
                <span class="ah-section-kicker"><c:out value="${historyKicker}"/></span>
                <h2 id="recordLogTitle"><c:out value="${historyListTitle}"/></h2>
                <p><c:out value="${historyDescription}"/></p>
            </div>
            <span class="ah-count" id="recordHistoryCount"><spring:message code="feature.common.count" text="{0}건" arguments="0"/></span>
        </div>

        <form id="recordHistorySearchForm" class="ah-search" role="search" autocomplete="off">
            <c:if test="${not printMode}">
            <label>
                <span><spring:message code="feature.history.filter.scope" text="자료 구분"/></span>
                <select id="recordScope">
                    <option value=""><spring:message code="feature.common.all" text="전체"/></option>
                    <option value="TECHNICAL_DATA"><spring:message code="feature.history.scope.technicalData" text="기술자료관리"/></option>
                    <option value="Documents"><spring:message code="feature.history.scope.documents" text="문서"/></option>
                </select>
            </label>
            </c:if>
            <label class="ah-search__keyword">
                <span><spring:message code="feature.common.search.keyword" text="통합검색"/></span>
                <input type="search" id="recordKeyword" maxlength="100"
                       placeholder="${historyKeywordPlaceholder}">
            </label>
            <div class="ah-search__actions">
                <button type="button" class="ah-button ah-button--ghost" id="recordResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i><spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="ah-button ah-button--primary" id="recordSearchButton">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i><spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div id="recordHistoryMessage" class="ah-message" role="status" aria-live="polite" hidden></div>

        <div class="ah-table-wrap">
            <table class="ah-table record-history-table" aria-label="${historyListTitle}">
                <colgroup>
                    <col class="rh-col-time"><col class="rh-col-user">
                    <c:choose>
                        <c:when test="${printMode}">
                            <col class="rh-col-type"><col class="rh-col-resource"><col class="rh-col-file">
                            <col class="rh-col-detail"><col class="rh-col-ip">
                        </c:when>
                        <c:otherwise>
                            <col class="rh-col-menu"><col class="rh-col-action">
                            <col class="rh-col-resource"><col class="rh-col-file-no">
                        </c:otherwise>
                    </c:choose>
                </colgroup>
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.history.column.time" text="일시"/></th>
                    <th scope="col"><spring:message code="feature.history.column.user" text="사용자"/></th>
                    <c:choose>
                        <c:when test="${printMode}">
                            <th scope="col"><spring:message code="feature.history.column.scope" text="자료 구분"/></th>
                            <th scope="col"><spring:message code="feature.history.column.resource" text="대상 자료"/></th>
                            <th scope="col"><spring:message code="feature.history.column.file" text="파일"/></th>
                            <th scope="col"><spring:message code="feature.history.column.detail" text="상세"/></th>
                            <th scope="col"><spring:message code="feature.history.column.ip" text="IP"/></th>
                        </c:when>
                        <c:otherwise>
                            <th scope="col"><spring:message code="feature.history.column.menu" text="메뉴"/></th>
                            <th scope="col"><spring:message code="feature.history.column.action" text="행위"/></th>
                            <th scope="col"><spring:message code="feature.history.column.document" text="문서"/></th>
                            <th scope="col"><spring:message code="feature.history.column.fileNumber" text="파일번호"/></th>
                        </c:otherwise>
                    </c:choose>
                </tr>
                </thead>
                <tbody id="recordHistoryTableBody">
                <tr class="ah-loading-row">
                    <td colspan="${printMode ? 7 : 6}"><span class="ah-spinner" aria-hidden="true"></span><spring:message
                            code="feature.history.loading" text="{0}을 불러오는 중입니다." arguments="${historyTitle}"/></td>
                </tr>
                </tbody>
            </table>
        </div>
        <footer class="ah-table-footer">
            <span><i class="icon-base ti tabler-info-circle" aria-hidden="true"></i><spring:message
                    code="feature.history.footer.limit" text="최대 1,000건을 최신순으로 조회합니다."/></span>
            <span><spring:message code="feature.history.footer.permission"
                                  text="이력관리 메뉴 권한이 있는 사용자만 조회할 수 있습니다."/></span>
        </footer>
    </section>
</main>
</body>
</html>
