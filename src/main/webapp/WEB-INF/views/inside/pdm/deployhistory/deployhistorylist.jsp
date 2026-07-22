<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Tab 승인 - CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>

<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;

// var gridParam;
// $(document).ready(function(){
// 	setGridParam();
// 	settingForm('${formInfo }');
// 	settingToolbar('${toolbarInfo }');
// 	settingGrid('${gridInfo }', gridParam);
// });

	function setGridParam(){
		gridParam = {
				gridId : 'itsSend',
				formId : 'formItsSend',
				url : '/inside/pdm/deployhistory/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice'
		};

		return gridParam;
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
<style>
</style>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="itsSend"/>
	</div>
</body>
</html>
