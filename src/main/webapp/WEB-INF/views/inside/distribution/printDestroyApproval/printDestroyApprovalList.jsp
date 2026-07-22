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
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;
var gridId = "gridPrintDestroyApprovalList";
var formId = "formPrintDestroyApproval";
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/distribution/printDestroyApproval/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice'
		}

		return gridParam;
	}

	function viewPrintDestroyApprovalPopup(destroyRequestNo){
		openDialogPopup("/inside/distribution/printDestroyApproval/approvalPopup", {destroyRequestNo: destroyRequestNo}, "popupDialog", 'xl', 620, true, 'popup-common');
	}

	function approval(){
		batchCall("A");
	}
	function reject(){
		batchCall("R");
	}

	function batchCall(saveType){
		var list = [];
		if($("#" + gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			console.log(data.hiddenDestroyRequestNo);
			list.push({destroyRequestNo: data.hiddenDestroyRequestNo});
		});
		var param = {list:list};
		param.saveType = saveType;
		console.log(param);
		callAjax( param, "/inside/distribution/printDestroyApproval/batchSaveApproval", batchCallback, 'json');
	}

	function batchCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridPrintDestroyApprovalList"/>
	</div>
</body>
</html>
