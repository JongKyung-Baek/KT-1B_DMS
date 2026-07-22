<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Collabhub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<style>
	.distribution-invoice-page #gbox_gridInsideUserList .ui-jqgrid-hdiv {
		overflow: visible !important;
	}
	.distribution-invoice-page #gbox_gridInsideUserList .ui-jqgrid-htable th,
	.distribution-invoice-page #gbox_gridInsideUserList .ui-jqgrid-htable th.ui-th-column {
		height: 37px !important;
		padding: 0 !important;
		vertical-align: middle !important;
	}
	.distribution-invoice-page #gbox_gridInsideUserList .ui-jqgrid-labels th > div,
	.distribution-invoice-page #gbox_gridInsideUserList .ui-jqgrid-labels th > div.ui-jqgrid-sortable {
		height: 37px !important;
		min-height: 37px !important;
		display: flex !important;
		align-items: center !important;
		justify-content: center !important;
		line-height: 1.2 !important;
		padding: 0 8px !important;
		box-sizing: border-box !important;
		white-space: nowrap !important;
		overflow: visible !important;
	}
</style>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var gridId = 'gridInsideUserList';
var formId = 'formInsideUser';

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/organizationmanage/insideuser/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		console.log(rowdata["requestPurpose"]);
		if("PRINT" == rowdata["requestPurpose"]){
			return '<a href="javascript: viewPrintDetail(\'' + cellValue + '\', \'' + rowdata['requestPurpose'] +'\')">' + cellValue + '</button>';
		}else{
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\')">' + cellValue + '</button>';
		}
	}

	function unlockAccount(){
		if($("#"+ gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else{
			$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#" + gridId).jqGrid('getRowData', item);
				//if(data.accountLockYn === 'Y'){
					var param = data;
					callAjax(param, '/inside/organizationmanage/insideuser/update', unlockAccountCallback);
				//}
			});
		}
	}
	function resetPwd(){
		if($("#"+ gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else{
			$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#" + gridId).jqGrid('getRowData', item);
				var param = data;
				callAjax(param, '/inside/organizationmanage/insideuser/resetPwd', resetPwdCallback);
			});
		}
	}

	/**
	 * 요청 후 결과 메시지 출력
	 * @param response
	 * @returns
	 */
	function unlockAccountCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){		//요청이 완료되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));					//요청이 실패했습니다
		}
	}


	function resetPwdCallback(response){
		if(response.success){
			infoMessage((function(){ var resetPwdMessage = '초기화가 완료되었습니다. <br/>초기 비밀번호는 "0000" 입니다.'; try { resetPwdMessage = g_msg('msg.resetPwd') || resetPwdMessage; } catch(e) { console.log(e); } return resetPwdMessage; })(), function(){		//초기 비밀번호는 0000 입니다
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));					//요청이 실패했습니다
		}
	}



	// 2023.09.11 기범추가 ( 신규 , 생성  버튼 생성 )
	// 신규 생성
	function registerUser(){
		openDialogPopup("/inside/organizationmanage/insideuser/registerUserPopup", {}, "popupDialog", 's', 500 );
	}
	// 수정
	// function editUser(){
	// 	openDialogPopup("/inside/organizationmanage/insideuser/editUser", {}, "popupDialog", 's', 360 );
	// }

	function formatUserNm(cellValue, options, rowdata, action){
		// console.log("rowdata >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", rowdata)
		return '<a onclick="openUserInfo(\''+rowdata["userCd"]+'\')">'+cellValue+'</a>';
	}


	function openUserInfo(userCd) {
		var param = {};

		if(undefined !== userCd) {
			param["userCd"] = userCd;
		}

		openDialogPopup("/inside/organizationmanage/insideuser/editUserPopup", param, "popupDialog", 's', 500);
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});


</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridInsideUserList"/>
	</div>
</body>
</html>
