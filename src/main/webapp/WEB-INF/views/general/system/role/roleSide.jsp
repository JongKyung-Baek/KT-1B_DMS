<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.system.role.title" text="사용자등급" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - KT-1B TDMS</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/role-vuexy.css?v=20260801.2" media="screen" />
<style>
	body { visibility: hidden; }
</style>
<script>
	var toolbarInfo = '${toolbarInfo}';
	var gridId = 'gridProductionApprovalList';
	var formId = 'formProductionApproval';
	var deptGridParam = undefined;
	var deptAssignedGridParam = undefined;
	var userGridParam = undefined;
	var userAssignedGridParam = undefined;
	var gridRoleDept = '${gridRoleDept}';
	var gridRoleDeptAssigned = '${gridRoleDeptAssigned}';
	var gridRoleUser = '${gridRoleUser}';
	var gridRoleUserAssigned = '${gridRoleUserAssigned}';

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container system-management-container');
	});

	function setGridParam(){
		deptGridParam = {
				gridId : "gridRoleDept",
				formId : "formRoleDept",
				url : '/general/system/role/deptList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				shrinkToFit : true,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		userGridParam = {
				gridId : "gridRoleUser",
				formId : "formRoleUser",
				url : '/general/system/role/userList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				shrinkToFit : true,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
	}
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/system/role/roleSide.js?v=20260801.2"></script>
</head>
<body>
	<main class="role-management-page" aria-labelledby="roleManagementTitle">
		<header class="system-role-page-heading">
			<div>
				<span class="system-role-kicker"><spring:message code="feature.system.role.kicker" text="USER GRADE MANAGEMENT"/></span>
				<h1 id="roleManagementTitle"><spring:message code="feature.system.role.title" text="사용자등급"/></h1>
				<p><spring:message code="feature.system.role.description" text="사용자등급을 만들고 부서 또는 사용자를 등급별로 배정합니다."/></p>
			</div>
			<div class="system-role-heading-chip">
				<i class="icon-base ti tabler-shield-check" aria-hidden="true"></i>
				<span><spring:message code="feature.system.role.headingChip" text="등급별 사용자 권한"/></span>
			</div>
		</header>

		<div class="role-management-layout">
			<section class="role-management-card role-group-card" aria-labelledby="roleGroupTitle">
				<header class="role-card-header role-group-card-header">
					<div class="role-card-heading">
						<span class="role-card-kicker"><spring:message code="feature.system.role.group.kicker" text="USER GRADES"/></span>
						<div class="role-card-title-row">
							<h2 id="roleGroupTitle"><spring:message code="feature.system.role.group.title" text="등급 목록"/></h2>
							<span id="managerCount" class="role-count-chip" aria-live="polite">0</span>
						</div>
						<p><spring:message code="feature.system.role.group.description" text="배정할 사용자등급을 선택하세요."/></p>
					</div>
					<div class="btnBox role-group-actions" aria-label="<spring:message code='feature.system.role.group.actionsAria' text='사용자등급 관리' javaScriptEscape='true'/>">
						<button type="button" class="role-action-button addGroupBtn" id="addGroup" onclick="addGroup()">
							<i class="icon-base ti tabler-plus" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.add" text="등록"/></span>
						</button>
						<button type="button" class="role-action-button modGroupBtn" id="modGroup" onclick="modGroup()">
							<i class="icon-base ti tabler-edit" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.edit" text="수정"/></span>
						</button>
						<button type="button" class="role-action-button role-action-button--danger delGroupBtn" id="delGroup" onclick="delGroup()">
							<i class="icon-base ti tabler-trash" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.delete" text="삭제"/></span>
						</button>
					</div>
				</header>
				<div class="role-group-list-wrap">
					<ul class="listBox role-group-list" aria-label="<spring:message code='feature.system.role.group.listAria' text='사용자등급 목록' javaScriptEscape='true'/>"></ul>
				</div>
			</section>

			<section class="role-management-card role-assignment-card" aria-labelledby="roleAssignmentTitle">
				<header class="role-card-header role-management-card-header">
					<div class="role-card-heading">
						<span class="role-card-kicker"><spring:message code="feature.system.role.assignment.kicker" text="GRADE MEMBERS"/></span>
						<div class="role-card-title-row">
							<h2 id="roleAssignmentTitle"><spring:message code="feature.system.role.assignment.title" text="등급 대상 배정"/></h2>
							<span id="selectedGroupName" class="role-selection-chip"><spring:message code="feature.system.role.assignment.selectGrade" text="등급을 선택하세요"/></span>
						</div>
						<p><spring:message code="feature.system.role.assignment.description" text="전체 목록에서 부서 또는 사용자를 선택해 등급에 포함합니다."/></p>
					</div>
					<div class="btnArea role-management-toolbar"></div>
				</header>
				<div class="role-management-card-body">
					<div id="tabs" class="role-tabs">
						<ul aria-label="<spring:message code='feature.system.role.assignment.tabsAria' text='배정 대상 유형' javaScriptEscape='true'/>">
							<li><a href="#tabs-dept"><i class="icon-base ti tabler-building" aria-hidden="true"></i><span><spring:message code="feature.system.role.tab.department" text="부서"/></span></a></li>
							<li><a href="#tabs-user"><i class="icon-base ti tabler-users" aria-hidden="true"></i><span><spring:message code="feature.system.role.tab.user" text="사용자"/></span></a></li>
						</ul>

						<div id="tabs-dept" class="gridArea role-grid-panel">
							<section class="total role-grid-section" aria-labelledby="availableDeptTitle">
								<header class="role-grid-heading">
									<div><span><spring:message code="feature.system.role.available.kicker" text="AVAILABLE"/></span><h3 id="availableDeptTitle"><spring:message code="feature.system.role.available.department" text="전체 부서"/></h3></div>
								</header>
								<form id="formRoleDept" class="role-search-form" onkeypress="return event.keyCode != 13;">
									<label class="role-search-field"><span><spring:message code="feature.system.role.field.departmentName" text="부서명"/></span><input type="text" name="deptNm" onKeyPress="if (event.keyCode==13){searchList(deptGridParam);}"/></label>
									<button type="button" class="searchBtn" onclick="searchList(deptGridParam)"><i class="icon-base ti tabler-search" aria-hidden="true"></i><span><spring:message code="feature.common.search" text="조회"/></span></button>
								</form>
								<div class="gridContainer role-grid-container"><table id="gridRoleDept"></table><div id="gridRoleDeptPager"></div></div>
							</section>
							<div class="changToolbar role-transfer-toolbar" aria-label="<spring:message code='feature.system.role.transfer.actionsAria' text='등급 배정 이동' javaScriptEscape='true'/>">
								<button type="button" class="addListBtn" onclick="addList()"><i class="icon-base ti tabler-chevron-right" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.assign" text="추가"/></span></button>
								<button type="button" class="delListBtn" onclick="delList()"><i class="icon-base ti tabler-chevron-left" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.unassign" text="제거"/></span></button>
							</div>
							<section class="selected role-grid-section" aria-labelledby="assignedDeptTitle">
								<header class="role-grid-heading"><div><span><spring:message code="feature.system.role.assigned.kicker" text="ASSIGNED"/></span><h3 id="assignedDeptTitle"><spring:message code="feature.system.role.assigned.department" text="배정된 부서"/></h3></div><span class="listCount role-count-chip" id="assignedDeptCount">0</span></header>
								<form id="formRoleDeptAssign" class="role-search-form" onkeypress="return event.keyCode != 13;">
									<label class="role-search-field"><span><spring:message code="feature.system.role.field.departmentName" text="부서명"/></span><input type="text" name="searchText" onKeyPress="if (event.keyCode==13){searchList2('assignedDept');}"/></label>
									<button type="button" class="searchBtn" onclick="searchList2('assignedDept')"><i class="icon-base ti tabler-search" aria-hidden="true"></i><span><spring:message code="feature.common.search" text="조회"/></span></button>
								</form>
								<div class="gridContainer role-grid-container"><table id="gridRoleDeptAssigned"></table></div>
							</section>
						</div>

						<div id="tabs-user" class="gridArea role-grid-panel">
							<section class="total role-grid-section" aria-labelledby="availableUserTitle">
								<header class="role-grid-heading"><div><span><spring:message code="feature.system.role.available.kicker" text="AVAILABLE"/></span><h3 id="availableUserTitle"><spring:message code="feature.system.role.available.user" text="전체 사용자"/></h3></div></header>
								<form id="formRoleUser" class="role-search-form" onkeypress="return event.keyCode != 13;">
									<label class="role-search-field"><span><spring:message code="feature.system.role.field.userName" text="사용자명"/></span><input type="text" name="userNm" onKeyPress="if (event.keyCode==13){searchList(userGridParam);}"/></label>
									<button type="button" class="searchBtn" onclick="searchList(userGridParam)"><i class="icon-base ti tabler-search" aria-hidden="true"></i><span><spring:message code="feature.common.search" text="조회"/></span></button>
								</form>
								<div class="gridContainer role-grid-container"><table id="gridRoleUser"></table><div id="gridRoleUserPager"></div></div>
							</section>
							<div class="changToolbar role-transfer-toolbar" aria-label="<spring:message code='feature.system.role.transfer.actionsAria' text='등급 배정 이동' javaScriptEscape='true'/>">
								<button type="button" class="addListBtn" onclick="addList()"><i class="icon-base ti tabler-chevron-right" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.assign" text="추가"/></span></button>
								<button type="button" class="delListBtn" onclick="delList()"><i class="icon-base ti tabler-chevron-left" aria-hidden="true"></i><span><spring:message code="feature.system.role.action.unassign" text="제거"/></span></button>
							</div>
							<section class="selected role-grid-section" aria-labelledby="assignedUserTitle">
								<header class="role-grid-heading"><div><span><spring:message code="feature.system.role.assigned.kicker" text="ASSIGNED"/></span><h3 id="assignedUserTitle"><spring:message code="feature.system.role.assigned.user" text="배정된 사용자"/></h3></div><span class="listCount role-count-chip" id="assignedUserCount">0</span></header>
								<form id="formRoleUserAssign" class="role-search-form" onkeypress="return event.keyCode != 13;">
									<label class="role-search-field"><span><spring:message code="feature.system.role.field.userName" text="사용자명"/></span><input type="text" name="searchText" onKeyPress="if (event.keyCode==13){searchList2('assignedUser');}"/></label>
									<button type="button" class="searchBtn" onclick="searchList2('assignedUser')"><i class="icon-base ti tabler-search" aria-hidden="true"></i><span><spring:message code="feature.common.search" text="조회"/></span></button>
								</form>
								<div class="gridContainer role-grid-container"><table id="gridRoleUserAssigned"></table></div>
							</section>
						</div>
					</div>
				</div>
			</section>
		</div>
	</main>
</body>
</html>
