<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/roleassign-vuexy.css" />
<script>
	var toolbarInfo = '${toolbarInfo}';

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/roleassign/roleSide.js"></script>
</head>
<body>
	<div class="row roleassign-page">
		<div class="col-xl-4 col-lg-5">
			<div class="card roleassign-card h-100">
				<div class="card-header d-flex justify-content-between align-items-center">
					<div>
						<h5 class="card-title mb-0"><spring:message code="label.managerGroup"/></h5>
					</div>
					<span id="managerCount" class="badge bg-label-primary rounded-pill">0</span>
				</div>
				<div class="card-body p-0">
					<ul class="listBox list-group list-group-flush role-group-list"></ul>
				</div>
			</div>
		</div>

		<div class="col-xl-8 col-lg-7">
			<div class="card roleassign-card h-100">
				<div class="card-header roleassign-card-header">
					<div class="btnArea roleassign-toolbar"></div>
				</div>
				<div class="card-body">
					<div id="menuTree" class="role-menu-tree"></div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
