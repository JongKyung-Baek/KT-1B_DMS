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
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common/commonApprovalStatus.js"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var g_gridId = 'gridOutsideSwApprovalStatusList';
var g_formId = 'formOutsideSwApprovalStatus';
		function viewDetail(lastDestroyRequestNo) {
			openDialogPopup("/outside/sw/approvalStatus/destroyStatusPopup"
					, {lastDestroyRequestNo: lastDestroyRequestNo}
					, "popupDialog", 's', 270, true, 'popup-common popup-destroy-status');
		}

	function formatFileCount(cellValue, options, rowdata, action){
		var text = cellValue + "건";

		if("0" == cellValue) {
			return text;
		}
		else {
			return '<a href="javascript: viewDetail(\'' + rowdata.lastDestroyRequestNo + '\')">' + text + '</button>';
		}
	}

	function setGridParam(){
		gridParam = {
				gridId : g_gridId,
				formId : g_formId,
				url : '/outside/sw/approvalStatus/selectList',
				pagerId: 'gridListPager',
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

	function formatOpenView(cellValue, options, rowdata, action){
		return '<a onclick="openFile(\'DISTRIBUTION\', \'SW\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	}
	function formatDownHistory(cellValue, options, rowdata, action){
		return '<a onclick="openDownHistoryPopup(\'' + rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\')">'+cellValue+'</a>';
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridOutsideSwApprovalStatusList"/>
	</div>
</body>
</html>
