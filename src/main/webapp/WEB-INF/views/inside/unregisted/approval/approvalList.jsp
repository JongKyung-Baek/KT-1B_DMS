<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
	var gridId = 'gridUnregistedApprovalList';
	var formId = 'formUnregistedApproval';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/unregisted/approval/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}
	
	function viewNoregDistributionApproval(requestNo){
		openDialogPopup("/inside/unregisted/approval/approvalPopup", {requestNo : requestNo}, "popupDialog", 'm', 750);
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
			list.push({requestNo: data.hiddenRequestNo});
		});
		var param = {list:list};
		param.saveType = saveType;
		callAjax( param, "/inside/unregisted/approval/batchSaveApproval", batchCallback, 'json');
	}
	
	function batchCallback(response){
		if(response.result.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}
	
</script>
</head>
<body>
	<custom:listTemplate gridId="gridUnregistedApprovalList"/>
</body>
</html>