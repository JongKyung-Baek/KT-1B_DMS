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
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/role-vuexy.css" media="screen" />
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
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

	function setGridParam(){
		deptGridParam = {
				gridId : "gridRoleDept",
				formId : "formRoleDept",
				url : '/inside/system/role/deptList',
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
				url : '/inside/system/role/userList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				shrinkToFit : true,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
	}
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/role/roleSide.js"></script>
</head>
<body>
	<div class="row role-management-page">
		<div class="col-xl-4 col-lg-5">
			<div class="card role-management-card h-100">
				<div class="card-header d-flex justify-content-between align-items-center">
					<div class="d-flex align-items-center gap-2">
						<h5 class="card-title mb-0"><spring:message code="label.roleGroup"/></h5>
						<span id="managerCount" class="badge bg-label-primary rounded-pill">0</span>
					</div>
					<div class="btnBox role-group-actions">
						<button type="button" class="addGroupBtn" id="addGroup" onclick="addGroup()" title="그룹 추가"></button>
						<button type="button" class="modGroupBtn" id="modGroup" onclick="modGroup()" title="그룹 수정"></button>
						<button type="button" class="delGroupBtn" id="delGroup" onclick="delGroup()" title="그룹 삭제"></button>
					</div>
				</div>
				<div class="card-body p-0">
					<ul class="listBox list-group list-group-flush role-group-list"></ul>
				</div>
			</div>
		</div>

		<div class="col-xl-8 col-lg-7">
			<div class="card role-management-card h-100">
				<div class="card-header role-management-card-header">
					<div class="btnArea role-management-toolbar"></div>
				</div>
				<div class="card-body role-management-card-body">
					<div id="tabs" class="role-tabs">
						<ul>
							<li><a href="#tabs-dept">부서</a></li>
							<li><a href="#tabs-user">사용자</a></li>
						</ul>
						<div id="tabs-dept" class="gridArea role-grid-panel">
							<div class="total role-grid-section">
								<div class="sbr role-section-header">
									<label><span class="gridTitle">전체 부서 목록</span></label>
									<form id="formRoleDept" onkeypress="return event.keyCode != 13;">
										<ul class="ibx">
											<li><label>부서명</label><input type="text" name="deptNm" onKeyPress="if (event.keyCode==13){searchList(deptGridParam);}"/></li>
										</ul>
										<div class="btnBox">
											<button type="button" class="ui-button ui-corner-all searchBtn" onclick="searchList(deptGridParam)">조회</button>
										</div>
									</form>
								</div>
								<div class="gridContainer role-grid-container">
									<table id="gridRoleDept"></table>
									<div id="gridRoleDeptPager"></div>
								</div>
							</div>
							<div class="changToolbar role-transfer-toolbar">
								<button type="button" class="addListBtn" onclick="addList()">추가</button>
								<button type="button" class="delListBtn" onclick="delList()">삭제</button>
							</div>
							<div class="selected role-grid-section">
								<div class="sbr role-section-header">
									<label><span class="gridTitle">선택된 목록</span><span class="listCount" id="assignedDeptCount">0</span></label>
									<form id="formRoleDeptAssign" onkeypress="return event.keyCode != 13;">
										<ul class="ibx">
											<li><label>부서명</label><input type="text" name="searchText" onKeyPress="if (event.keyCode==13){searchList2('assignedDept');}"/></li>
										</ul>
										<div class="btnBox">
											<button type="button" class="ui-button ui-corner-all searchBtn" onclick="searchList2('assignedDept')">조회</button>
										</div>
									</form>
								</div>
								<div class="gridContainer role-grid-container">
									<table id="gridRoleDeptAssigned"></table>
								</div>
							</div>
						</div>

						<div id="tabs-user" class="gridArea role-grid-panel">
							<div class="total role-grid-section">
								<div class="sbr role-section-header">
									<label><span class="gridTitle">전체 사용자 목록</span></label>
									<form id="formRoleUser" onkeypress="return event.keyCode != 13;">
										<ul class="ibx">
											<li><label>사용자명</label><input type="text" name="userNm" onKeyPress="if (event.keyCode==13){searchList(userGridParam);}"/></li>
										</ul>
										<div class="btnBox">
											<button type="button" class="ui-button ui-corner-all searchBtn" onclick="searchList(userGridParam)">조회</button>
										</div>
									</form>
								</div>
								<div class="gridContainer role-grid-container">
									<table id="gridRoleUser"></table>
									<div id="gridRoleUserPager"></div>
								</div>
							</div>
							<div class="changToolbar role-transfer-toolbar">
								<button type="button" class="addListBtn" onclick="addList()">추가</button>
								<button type="button" class="delListBtn" onclick="delList()">삭제</button>
							</div>
							<div class="selected role-grid-section">
								<div class="sbr role-section-header">
									<label><span class="gridTitle">선택된 목록</span><span class="listCount" id="assignedUserCount">0</span></label>
									<form id="formRoleUserAssign" onkeypress="return event.keyCode != 13;">
										<ul class="ibx">
											<li><label>사용자명</label><input type="text" name="searchText" onKeyPress="if (event.keyCode==13){searchList2('assignedUser');}"/></li>
										</ul>
										<div class="btnBox">
											<button type="button" class="ui-button ui-corner-all searchBtn" onclick="searchList2('assignedUser')">조회</button>
										</div>
									</form>
								</div>
								<div class="gridContainer role-grid-container">
									<table id="gridRoleUserAssigned"></table>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
