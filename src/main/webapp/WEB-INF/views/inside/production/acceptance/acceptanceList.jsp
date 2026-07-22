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
				gridId : 'gridProductionAcceptance',
				formId : 'formProductionAcceptance',
				url : '/inside/production/acceptance/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
// 		return '<a href="javascript: accept()">' + cellValue + '</button>';
		return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectId'] +'\', \'' + rowdata['objectType'] +'\')">' + cellValue + '</button>';
	}

	function viewDetail(requestNo, objectId, objectType) {
		openDialogPopup("/inside/production/acceptance/acceptancePopup", {requestNo: requestNo, objectId : objectId, objectType : objectType}, "popupDialog", 'l', 620);
	}

	function accept(){
		if($("#gridProductionAcceptance").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else {
			var aJsonArray = new Array();
			$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
				var aJson = new Object();
				aJson.requestNo = data.requestNo;
				aJson.objectId = data.objectId;
				aJsonArray.push(aJson);
			});
			openDialogPopup("/inside/production/acceptance/acceptancePopup", {list:JSON.stringify(aJsonArray)}, "popupDialog", 'l', 620);
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionAcceptance"/>
</body>
</html>
