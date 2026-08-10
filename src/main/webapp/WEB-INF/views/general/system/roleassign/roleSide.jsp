<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.system.roleassign.title" text="메뉴권한 배정" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/roleassign-vuexy.css?v=20260802.2" />
<script>
var toolbarInfo = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${toolbarInfo}</spring:escapeBody>';

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container system-management-container');
	});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/system/roleassign/roleSide.js?v=20260801.2"></script>
</head>
<body>
	<main class="roleassign-page" aria-labelledby="roleassignPageTitle">
		<header class="roleassign-page-heading">
			<div>
				<span class="roleassign-kicker"><spring:message code="feature.system.roleassign.kicker" text="MENU PERMISSION ASSIGNMENT"/></span>
				<h1 id="roleassignPageTitle"><spring:message code="feature.system.roleassign.title" text="메뉴권한 배정"/></h1>
				<p><spring:message code="feature.system.roleassign.description" text="사용자등급별로 접근 가능한 메뉴를 선택하고 저장합니다."/></p>
			</div>
			<div class="roleassign-heading-chip">
				<i class="icon-base ti tabler-lock-access" aria-hidden="true"></i>
				<span><spring:message code="feature.system.roleassign.headingChip" text="등급별 메뉴 접근통제"/></span>
			</div>
		</header>

		<div class="roleassign-layout">
			<section class="roleassign-card roleassign-grade-card" aria-labelledby="roleassignGradeTitle">
				<header class="roleassign-card-header roleassign-grade-header">
					<div>
						<span class="roleassign-card-kicker"><spring:message code="feature.system.roleassign.grade.kicker" text="USER GRADES"/></span>
						<div class="roleassign-title-row">
							<h2 id="roleassignGradeTitle"><spring:message code="feature.system.roleassign.grade.title" text="사용자등급"/></h2>
							<span id="managerCount" class="roleassign-count-chip" aria-live="polite">0</span>
						</div>
						<p><spring:message code="feature.system.roleassign.grade.description" text="메뉴권한을 설정할 등급을 선택하세요."/></p>
					</div>
				</header>
				<div class="roleassign-grade-list-wrap">
					<ul class="listBox role-group-list" aria-label="<spring:message code='feature.system.roleassign.grade.listAria' text='사용자등급 목록' javaScriptEscape='true'/>"></ul>
				</div>
			</section>

			<section class="roleassign-card roleassign-permission-card" aria-labelledby="roleassignMenuTitle">
				<header class="roleassign-card-header roleassign-permission-header">
					<div>
						<span class="roleassign-card-kicker"><spring:message code="feature.system.roleassign.menu.kicker" text="MENU ACCESS"/></span>
						<div class="roleassign-title-row">
							<h2 id="roleassignMenuTitle"><spring:message code="feature.system.roleassign.menu.title" text="접근 메뉴 선택"/></h2>
							<span id="selectedRoleName" class="roleassign-selection-chip"><spring:message code="feature.system.roleassign.menu.selectGrade" text="등급을 선택하세요"/></span>
							<span class="roleassign-count-chip roleassign-count-chip--green"><span id="selectedMenuCount">0</span><spring:message code="feature.system.roleassign.menu.selectedSuffix" text="개 선택"/></span>
						</div>
						<p><spring:message code="feature.system.roleassign.menu.description" text="체크된 메뉴와 하위메뉴가 해당 등급의 메뉴에 표시됩니다."/></p>
					</div>
					<div class="btnArea roleassign-toolbar"></div>
				</header>
				<div class="roleassign-card-body">
					<div class="roleassign-guide" role="note">
						<i class="icon-base ti tabler-info-circle" aria-hidden="true"></i>
						<span><spring:message code="feature.system.roleassign.menu.guide" text="상위메뉴를 선택하면 하위메뉴도 함께 선택됩니다. 변경 후 저장 버튼을 눌러 적용하세요."/></span>
					</div>
					<div id="menuTree" class="role-menu-tree" aria-label="<spring:message code='feature.system.roleassign.menu.treeAria' text='메뉴권한 선택 트리' javaScriptEscape='true'/>"></div>
				</div>
			</section>
		</div>
	</main>
</body>
</html>
