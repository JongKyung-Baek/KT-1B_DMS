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
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/commonapproval/commonApproval.js"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;
var gridId = 'gridDistributionApproval';
var formId = 'formDistributionApproval';

	$(function() {
		addPassBtn();
	});

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/distribution/approval/selectList',
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

	function viewDistributionApproval(requestNo, approvalLineId, currentProcessSeqNo, objectType){
		var popupHeight = Math.min($(window).height() - 140, 640);
		openDialogPopup("/inside/distribution/approval/approvalPopup", {requestNo: requestNo, approvalLineId: approvalLineId, currentProcessSeqNo: currentProcessSeqNo, objectType: objectType}, "popupDialog", 'l', popupHeight, true, 'popup-common popup-distribution-approval');
	}

	function addPassBtn() {
		var html = '<button class="ui-button ui-corner-all" onclick="pass();">이관</button>';
		if('Y' === PASS_USE_YN) {
			$(".contentArea .distribution-invoice-layout > .btnArea > .right, .contentArea > .btnArea > .right").append(html);
		}
	}

	function pass() {
		var requestNo = [];

		if($("#" + gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}

		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);

			requestNo.push(data.hiddenRequestNo);
		});

		openDialogPopup("/inside/distribution/approval/passPopup", {requestNo: requestNo.join(',')}, "popupDialog", 's', 200);
	}
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridDistributionApproval"/>
	</div>
</body>
</html>
