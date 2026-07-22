<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<style>
	.distribution-invoice-page #gview_gridDisposalAcceptanceList .ui-jqgrid-htable th {
		height: 34px;
		padding-top: 2px;
		padding-bottom: 2px;
		vertical-align: middle;
	}

	
	.distribution-invoice-page #gview_gridDisposalAcceptanceList .ui-jqgrid-htable th div:not(.clearfix) {
		height: 30px;
		min-height: 28px;
		width: 100%;
		margin: 0;
		padding-top: 0;
		padding-bottom: 0;
		line-height: 1.2;
		display: flex;
		align-items: center;
		justify-content: center;
		text-align: center;
	}
</style>

<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;
var gridId = 'gridDisposalAcceptanceList'
var formId = 'formDisposalAcceptance'
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/distribution/disposalacceptance/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false,
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue +  '\')">' + cellValue + '</button>';
	}

	function viewDetail(destroyRequestNo) {
		openDialogPopup("/inside/distribution/disposalacceptance/approvalPopup",{destroyRequestNo: destroyRequestNo}, "popupDialog", 'xl', 750, true, 'popup-common');
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridDisposalAcceptanceList"/>
	</div>
</body>
</html>
