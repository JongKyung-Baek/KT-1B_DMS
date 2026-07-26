<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.organization.user.browserTitle" text="내부 사용자 관리" var="pageTitle"/>
<spring:message code="feature.organization.user.resultsAria" text="내부 사용자 검색 및 목록" var="resultsAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/organization-management.css?v=20260726.2" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/organization-management.js?v=20260726.1"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var gridId = 'gridInsideUserList';
var formId = 'formInsideUser';

	function organizationText(key, fallback) {
		return window.SdmsI18n && typeof window.SdmsI18n.t === 'function'
			? window.SdmsI18n.t(key, fallback)
			: fallback;
	}

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
			alertMessage(organizationText('feature.common.noSelection', '선택된 데이터가 없습니다.'), function(){
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
			alertMessage(organizationText('feature.common.noSelection', '선택된 데이터가 없습니다.'), function(){
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
			infoMessage(organizationText('feature.common.requestCompleted', '요청이 완료되었습니다.'), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(organizationText('feature.common.requestFailed', '요청이 실패했습니다.'));
		}
	}


	function resetPwdCallback(response){
		if(response.success){
			infoMessage((function(){
				var resetPwdMessage = '초기화가 완료되었습니다. <br/>초기 비밀번호는 "0000" 입니다.';
				if(window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
					resetPwdMessage = window.SdmsI18n.t(
						'feature.organization.user.passwordReset.completed',
						resetPwdMessage
					);
				}
				return resetPwdMessage;
			})(), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(organizationText('feature.common.requestFailed', '요청이 실패했습니다.'));
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
	<div class="distribution-invoice-page organization-management-page">
		<section class="organization-management-results-card" aria-label="${resultsAria}">
			<custom:listTemplateInvoice gridId="gridInsideUserList"/>
		</section>
	</div>
</body>
</html>
