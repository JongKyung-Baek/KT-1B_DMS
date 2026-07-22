<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- CR요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CR요청 - CR - CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;
	function openDialog() {
		openDialogPopup("/outside/cr/request/requestPopup", {}, "popupDialog", 'l', 800, true, 'popup-common');
	}
	
		function setGridParam(){
			gridParam = {
					gridId : 'gridOutsideCrRequestList',
					formId : 'formOutsideCrRequest',
					url : '/outside/cr/request/selectList',
					pagerId: 'gridListPager',
					size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
					page : 1,
					multiSelect : false,
					numbering : false,
					layoutMode : 'invoice'
			}

			return gridParam;
	}

	function formatCrNo(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue + '\')">' + cellValue + '</button>';
	}

	function formatCrStatusNm(cellValue, options, rowdata, action){
		return '<span class="tdBlue">' + cellValue + '</span>';
	}

		function viewDetail(crNo){
			openDialogPopup("/outside/cr/request/requestStatusPopup", {crNo: crNo}, "popupDialog", "l", 750, true, 'popup-common popup-request-status');
		}

		$(function() {
			$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
		});
	</script>
	<style>
	</style>
</head>
<body>
		<div class="distribution-invoice-page">
			<custom:listTemplateInvoice gridId="gridOutsideCrRequestList"/>
		</div>
</body>
</html>
