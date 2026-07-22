<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
	var gridId = 'gridProductionApprovalList';
	var formId = 'formProductionApproval';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/production/approval/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		console.log(rowdata["requestPurpose"]);
		if("PRINT" == rowdata["requestPurpose"]){
			return '<a href="javascript: viewPrintDetail(\'' + cellValue + '\', \'' + rowdata['requestPurpose'] +'\')">' + cellValue + '</button>';
		}else {
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\', \'' + rowdata['requestPurpose'] +'\')">' + cellValue + '</button>';
		}
	}

	function viewDetail(requestNo, objectType, requestPurpose){
		if(requestPurpose == "NEW"){
			openDialogPopup("/inside/production/approval/approvalPopup", {requestNo: requestNo, objectType: objectType}, "popupDialog", 'l', 725);
		}else{
			openDialogPopup("/inside/production/approval/replaceApprovalPopup", {requestNo: requestNo, objectType: objectType}, "popupDialog", 'l', 525);
		}
	}

	function viewPrintDetail(requestNo, requestPurpose) {
// 		openDialogPopup('/inside/production/requeststatus/printApprovalPopup', {requestNo : requestNo}, 'popupDialog', 'l', '550');
		openDialogPopup("/inside/production/approval/printApprovalPopup", {requestNo: requestNo, requestPurpose: requestPurpose}, "popupDialog", 'l', 530);
	}

	function approvalAll(){
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
			list.push({requestNo: data.hiddenRequestNo, objectType: data.objectType, requestPurpose: data.requestPurpose});
		});
		var param = {list:list};
		param.requestNo = $('#'+gridId).jqGrid('getCell', 1, 'hiddenRequestNo');
		param.saveType = saveType;
		callAjax( param, "/inside/production/approval/batchSaveApproval", batchCallback, 'json');
	}

	function batchCallback(response){
		if(response.result.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else if(response.result.failReason === 'destroyInProgress'){
			alertMessage(g_msg("msg.destroyInProgress", response.result.data));
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionApprovalList"/>
</body>
</html>