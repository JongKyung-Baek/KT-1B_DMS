<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
	function setGridParam(){
		gridParam = {
				gridId : 'gridProductionRequestStatusList',
				formId : 'formProductionRequestStatus',
				url : '/inside/production/requeststatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\', \'' + rowdata["deployTypeCd"] + '\')">' + cellValue + '</a>';
// 			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\')">' + cellValue + '</a>';
	}

	function viewDetail(requestNo, objectType, deployTypeCd) {
		if(deployTypeCd === 'PRINT') {
			openDialogPopup('/inside/production/requeststatus/printRequestStatusPopup', {requestNo : requestNo, objectType: objectType}, 'popupDialog', 'l', '520');
		} else {
			openDialogPopup('/inside/production/requeststatus/requestStatusPopup', {requestNo : requestNo, objectType: objectType}, 'popupDialog', 'l', '750');
		}
	}

	function printApproval() {
		var requestNo = '';
		if($("#"+gridParam.gridId).getGridParam('selarrrow').length > 1){
			alertMessage(g_msg('msg.onlyOneData'), function() {				//한 번에 하나의 데이터만 선택해 주시기 바랍니다.
				$(this).dialog("close");
			});
			return false;
		}else {
			$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
				requestNo = data.requestNo;
			});
			openDialogPopup('/inside/production/requeststatus/printApprovalPopup', {requestNo : requestNo}, 'popupDialog', 'l', '540');
		}

	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionRequestStatusList"/>
</body>
</html>