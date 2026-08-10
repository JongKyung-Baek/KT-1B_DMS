<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.audit.browserTitle" text="접근·감사이력" var="pageTitle"/>
<spring:message code="feature.audit.results.aria" text="접근·감사이력 검색 및 목록" var="resultsAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/audit-log.css?v=20260802.2" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/distribution/acceptance/common-form-vuexy.js"></script>
<script>
    window.USE_ACCEPTANCE_VUEXY_FORM = true;
    var gridId = 'gridInsideAuditLogList';
    var formId = 'formInsideAuditLog';
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/organizationmanage/auditlog/auditlogList.js?v=20260801.1"></script>
</head>
<body>
    <div class="distribution-invoice-page audit-log-page">
        <section class="audit-log-results-card" aria-label="${resultsAria}">
            <custom:listTemplateInvoice gridId="gridInsideAuditLogList"/>
        </section>
    </div>
</body>
</html>
