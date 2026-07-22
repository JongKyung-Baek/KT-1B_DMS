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

		function setGridParam(){
			gridParam = {
					gridId : 'gridOutsideUserStatusList',
					formId : 'formOutsideUserStatus',
					url : '/outside/user/status/selectList',
					size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
					page : 1,
					pagerId: 'gridListPager',
					multiSelect : false,
					numbering : false,
					layoutMode : 'invoice',
					fillColumns : true
			}
	
			return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + rowdata["requestNo"] + '\')">' + cellValue + '</button>';
	}

			function viewDetail(requestNo){
				openDialogPopup("/outside/user/status/statusPopup"
						, {requestNo: requestNo}
						, "popupDialog", 'm', 345, true, 'popup-common popup-request-status');
			}

		$(function() {
			$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
		});
	</script>
</head>
<body>
		<div class="distribution-invoice-page">
			<custom:listTemplateInvoice gridId="gridOutsideUserStatusList"/>
		</div>
</body>
</html>
