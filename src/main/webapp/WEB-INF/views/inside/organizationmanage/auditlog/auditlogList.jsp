<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.audit.browserTitle" text="운영 감사 로그" var="pageTitle"/>
<spring:message code="feature.audit.results.aria" text="감사로그 검색 및 목록" var="resultsAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - KT-1B DMS</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/audit-log.css?v=20260726.5" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
    window.USE_ACCEPTANCE_VUEXY_FORM = true;
    var gridId = 'gridInsideAuditLogList';
    var formId = 'formInsideAuditLog';
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/auditlog/auditlogList.js?v=20260726.2"></script>
</head>
<body>
    <c:set var="summary" value="${dashboardSummary}" />
    <div class="distribution-invoice-page audit-log-page">
        <div class="audit-log-overview">
            <section class="audit-log-summary" aria-labelledby="auditLogSummaryTitle">
                <div class="audit-log-section-heading">
                    <div>
                        <span class="audit-log-section-kicker"><spring:message code="feature.audit.summary.kicker" text="오늘"/></span>
                        <h2 id="auditLogSummaryTitle"><spring:message code="feature.audit.summary.title" text="오늘 요약"/></h2>
                    </div>
                    <span class="audit-log-section-note"><spring:message code="feature.audit.summary.period"
                                                                        text="오늘 00:00부터 현재까지"/></span>
                </div>
                <div class="audit-log-summary__grid">
                    <article class="audit-log-metric audit-log-metric--total">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-activity"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label"><spring:message code="feature.audit.summary.total.label"
                                                                                 text="전체 이벤트"/></span>
                            <strong><fmt:formatNumber value="${empty summary.totalToday ? 0 : summary.totalToday}" /></strong>
                            <span class="audit-log-metric__note"><spring:message code="feature.audit.summary.total.note"
                                                                                text="요청·인증·보안 행위 합계"/></span>
                        </div>
                    </article>
                    <article class="audit-log-metric audit-log-metric--users">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-users"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label"><spring:message code="feature.audit.summary.users.label"
                                                                                 text="활동 사용자"/></span>
                            <strong><fmt:formatNumber value="${empty summary.activeUsersToday ? 0 : summary.activeUsersToday}" /></strong>
                            <span class="audit-log-metric__note"><spring:message code="feature.audit.summary.users.note"
                                                                                text="오늘 기록된 고유 사용자"/></span>
                        </div>
                    </article>
                </div>
            </section>

        </div>

        <section class="audit-log-results-card" aria-label="${resultsAria}">
            <custom:listTemplateInvoice gridId="gridInsideAuditLogList"/>
        </section>
    </div>
</body>
</html>
