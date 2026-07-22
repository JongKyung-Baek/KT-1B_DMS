<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
		window.USE_ACCEPTANCE_VUEXY_FORM = true;

			function openDialog() {
				openDialogPopup("/outside/drawing/request/requestPopup",{}, "popupDialog", 'l', 725, true, 'popup-common popup-request');
			}
	
		function setGridParam(){
			gridParam = {
					gridId : 'gridOutsideUserInformationList',
					formId : 'formOutsideUserInformation',
					url : '/outside/user/information/selectList',
					pagerId: 'gridListPager',
					size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
					page : 1,
					multiSelect : false,
					numbering : false,
					layoutMode : 'invoice',
					fillColumns : true
			}

		return gridParam;
	}

	function formatUserNm(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + rowdata["userCd"] + '\')">' + cellValue + '</button>';
	}

	function requestPopup(){
		openDialogPopup("/outside/user/information/requestPopup", {userCd:''} , "popupDialog", 'm', 540, true, 'popup-common');
	}

	function viewDetail(userCd){
		openDialogPopup("/outside/user/information/requestPopup"
				, {userCd: userCd}
				, "popupDialog", 'm', 480, true, 'popup-common');
	}

	function userModPopup(){
		openDialogPopup("/outside/user/information/userModPopup", {} , "popupDialog", 'm', 440, true, 'popup-common');
	}

		$(function() {
			$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
		});

</script>
</head>
<body>
		<div class="distribution-invoice-page">
			<custom:listTemplateInvoice gridId="gridOutsideUserInformationList"/>
		</div>
</body>
</html>
