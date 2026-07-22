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
				gridId : 'gridProductionHistoryList',
				formId : 'formProductionHistory',
				url : '/inside/production/history/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false
		}

		return gridParam;
	}

	function formatterRequestNo(cellValue, options, rowdata, action){
		console.log(rowdata);
// 		if(rowdata[''])
		return '<a onclick="viewDetail(\''+rowdata['requestNo']+'\')">'+cellValue+'</a>';
	}

	function viewDetail(requestNo){
		openDialogPopup("/inside/production/history/historyDetailPopup", {requestNo: requestNo}, "popupDialog", 'l', 745);
	}


// 	function exportExcel(){
// 		openDialogPopup("/inside/production/history/historyDetail2Popup"
// 				, {}
// 				, "popupDialog", 'l', 800);
// 	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionHistoryList"/>
</body>
</html>