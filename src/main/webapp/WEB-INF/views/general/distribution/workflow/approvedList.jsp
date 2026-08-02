<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.distributionWorkflow.page.approved.title" text="승인완료 배포목록" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - KT-1B TDMS</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-workflow.css?v=20260802.2" media="screen">
<script>
window.distributionWorkflowPage = {
    contextPath: '${pageContext.request.contextPath}',
    mode: 'approved',
    openCreateOnLoad: false
};
</script>
</head>
<body>
<main class="distribution-workflow-page" data-workflow-mode="approved" aria-labelledby="workflowPageTitle">
    <section class="dw-card">
        <header class="dw-card__header">
            <div>
                <span class="dw-section-kicker" data-i18n-key="feature.distributionWorkflow.page.approved.kicker">
                    <spring:message code="feature.distributionWorkflow.page.approved.kicker" text="APPROVED DISTRIBUTIONS"/>
                </span>
                <h2 id="workflowPageTitle">${pageTitle}</h2>
                <p data-i18n-key="feature.distributionWorkflow.page.approved.description">
                    <spring:message code="feature.distributionWorkflow.page.approved.description"
                                    text="승인이 완료되어 향후 기술자료배포시스템으로 연계할 배포목록입니다."/>
                </p>
            </div>
            <span class="dw-count" id="workflowCount">0<spring:message code="feature.common.countSuffix" text="건"/></span>
        </header>

        <form class="dw-search dw-search--single" id="workflowSearchForm" role="search" autocomplete="off">
            <label class="dw-search__keyword">
                <span><spring:message code="feature.distributionWorkflow.filter.keyword" text="통합검색"/></span>
                <input type="search" id="workflowKeyword" maxlength="100"
                       placeholder="<spring:message code='feature.distributionWorkflow.filter.approvedKeywordPlaceholder' text='요청번호, 제목, 요청자, 승인자 검색'/>">
            </label>
            <div class="dw-search__actions">
                <button type="button" class="dw-button dw-button--ghost" id="workflowResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i>
                    <spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="dw-button dw-button--primary">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                    <spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div class="dw-alert" id="workflowPageMessage" role="status" aria-live="polite" hidden></div>
        <div class="dw-table-wrap">
            <table class="dw-table dw-request-table" aria-label="${pageTitle}">
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.requestNo" text="요청번호"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.title" text="요청 제목"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.distributionTarget" text="배포대상"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.distributionPeriod" text="배포기간"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.itemCount" text="파일건수"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.approver" text="승인자"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.decidedAt" text="승인일"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.actions" text="관리"/></th>
                </tr>
                </thead>
                <tbody id="workflowTableBody"></tbody>
            </table>
        </div>
        <footer class="dw-table-footer">
            <span><i class="icon-base ti tabler-info-circle" aria-hidden="true"></i>
                <spring:message code="feature.distributionWorkflow.footer.externalHold"
                                text="현재는 배포목록만 생성하며 외부 시스템으로 자동 전송하지 않습니다."/>
            </span>
        </footer>
    </section>
    <%@ include file="workflowDialog.jspf" %>
</main>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/general/distribution/workflow/distribution-workflow.js?v=20260801.4"></script>
</body>
</html>
