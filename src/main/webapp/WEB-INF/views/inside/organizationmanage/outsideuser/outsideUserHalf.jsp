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

	var gridCompanyId = 'gridOutsideCompanyList';
	var formCompanyId = 'formOutsideCompany';
	var gridUserId = 'gridOutsideUserList';
	var formUserId = 'formOutsideUser';
	var gridParam;
	var gridUserParam;
	var selectedCompanyCd = '';

	function setGridParam(){
		gridParam = {
				gridId : gridCompanyId,
				formId : formCompanyId,
				url : '/inside/organizationmanage/outsideuser/selectCompanyList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}

		gridUserParam = {
				gridId : gridUserId,
				formId : formUserId,
				url : '/inside/organizationmanage/outsideuser/selectUserList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}
	}

	function unlockAccount() {
		var selrow = $('#'+gridUserId).jqGrid('getGridParam', 'selarrrow');
		var i=0;
		var userCd = [];

		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}

		for(i=0; i<selrow.length; i++) {
			var rowdata = $("#" + gridUserId).jqGrid('getRowData', selrow[i]);
			userCd.push(rowdata.userCd);
		}

		var param = {
				userCd: userCd.join(',')
		}

		callAjax(param, '/inside/organizationmanage/outsideuser/unlockAccount', function(response){
			if(response.success) {
 				alertMessage(g_msg('msg.completeUnlock'), function() {
					searchList(gridUserId);
					$(this).dialog("close");
				});
			}
			else {
				alertMessage(g_msg('msg.failUnlock'), function() {
				});

			}
		})
	}

	function onCustomSelectRow(rowid, status, e) {
		if(e.currentTarget.id == gridCompanyId) {
			var rowdata = $("#" + gridCompanyId).jqGrid('getRowData', rowid);

			$("#formOutsideUser #companyCd").val(rowdata.companyCd);

			searchList(gridUserParam);
		}
	}

	function searchCompanyList() {
		searchList(gridParam);
		$("#formOutsideUser #companyCd").val("");
		searchList(gridUserParam);
	}

	/* setGridParam();
	settingForm('${formInfo }');
	settingToolbar(${toolbarInfo });
	settingGrid('${gridInfo }', gridParam, 'gridParam');
	searchList(gridParam); */

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
		setGridParam();
		settingToolbar(${toolbarOutsideCompany }, $(".btnAreaLeft"));
		settingToolbar(${toolbarOutsideUser }, $(".btnAreaRight"));
		settingForm('${formOutsideCompany }', $(".sbrLeft"));
		settingForm('${formOutsideUser }', $(".sbrRight"));
		settingGrid('${gridOutsideCompanyList }', gridParam, 'gridParam');
		settingGrid('${gridOutsideUserList }', gridUserParam, 'gridUserParam');
	});

	function formatCompanyNm(cellValue, options, rowdata, action){
		return '<a onclick="openCompanyInfo(\''+rowdata["companyCd"]+'\')">'+cellValue+'</a>';
	}


	function formatUserNm(cellValue, options, rowdata, action){
		return '<a onclick="openUserInfo(\'' + rowdata["userCd"] + '\')">' + cellValue + '</a>';
	}


	function openCompanyInfo(companyCd) {
		var param = {};
		var type = "Add";

		if(undefined !== companyCd) {
			selectedCompanyCd = companyCd;
			param["companyCd"] = companyCd;
			type = "Mod";
		}

		var popupHeight = Math.min($(window).height() - 140, 330);
		openDialogPopup("/inside/organizationmanage/outsideuser/company" + type + "Popup", param, "popupDialog", 'm', popupHeight, true, 'popup-common popup-company');
	}

	function openUserInfo(userCd) {
		// console.log("rowdata >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " , rowdata)
		var param = {};

		if(undefined !== userCd) {
			param["userCd"] = userCd;
		}

		var userPopupHeight = Math.min($(window).height() - 140, 320);
		openDialogPopup("/inside/organizationmanage/outsideuser/userModPopup", param, "popupDialog", 's', userPopupHeight, true, 'popup-common popup-user');
	}

	function addUser() {
		var param = {};

		var userPopupHeight = Math.min($(window).height() - 140, 320);
		openDialogPopup("/inside/organizationmanage/outsideuser/userAddPopup", param, "popupDialog", 's', userPopupHeight, true, 'popup-common popup-user');
	}




</script>
<style>
	.distribution-invoice-page.outside-user-page .outside-user-layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
		gap: 1rem;
		align-items: start;
	}

	.distribution-invoice-page.outside-user-page .outside-user-panel {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		min-width: 0;
	}

	.distribution-invoice-page.outside-user-page .outside-user-card {
		border: 0;
		min-width: 0;
	}

	.distribution-invoice-page.outside-user-page .outside-user-card .card-body {
		padding: 1rem 1.25rem;
	}

	.distribution-invoice-page.outside-user-page .sbr {
		background: transparent;
		border-bottom: 0;
		padding: 0;
	}

	.distribution-invoice-page.outside-user-page .gridArea {
		min-width: 0;
	}

	.distribution-invoice-page.outside-user-page .gridContainer {
		min-width: 0;
	}

	.distribution-invoice-page.outside-user-page .center {
		display: none;
	}

	@media (max-width: 991.98px) {
		.distribution-invoice-page.outside-user-page .outside-user-layout {
			grid-template-columns: 1fr;
		}
	}
</style>
</head>
<body>
	<div class="distribution-invoice-page outside-user-page">
		<div class="outside-user-layout">
			<div class="outside-user-panel">
				<div class="card distribution-filter-card outside-user-card">
					<div class="card-body">
						<div class="sbr sbrLeft"></div>
					</div>
				</div>
				<div class="btnArea btnAreaLeft"></div>
				<div class="card distribution-grid-card outside-user-card">
					<div class="card-datatable table-responsive">
						<div class="gridArea">
							<div class="gridContainer">
								<table id="gridOutsideCompanyList"></table>
								<div id="gridOutsideCompanyListPager"></div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="outside-user-panel">
				<div class="card distribution-filter-card outside-user-card">
					<div class="card-body">
						<div class="sbr sbrRight"></div>
					</div>
				</div>
				<div class="btnArea btnAreaRight"></div>
				<div class="card distribution-grid-card outside-user-card">
					<div class="card-datatable table-responsive">
						<div class="gridArea">
							<div class="gridContainer">
								<table id="gridOutsideUserList"></table>
								<div id="gridOutsideUserListPager"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
