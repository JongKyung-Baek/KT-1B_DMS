<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
    window.USE_ACCEPTANCE_VUEXY_FORM = true;
    var gridId = 'gridInsideAuditLogList';
    var formId = 'formInsideAuditLog';
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/auditlog/auditlogList.js"></script>
</head>
<body>
    <div class="distribution-invoice-page">
        <custom:listTemplateInvoice gridId="gridInsideAuditLogList"/>
    </div>
</body>
</html>
