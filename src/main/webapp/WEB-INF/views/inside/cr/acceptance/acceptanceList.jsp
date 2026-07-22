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
				gridId : 'gridCrAcceptanceList',
				formId : 'formCrAcceptance',
				url : '/inside/cr/acceptance/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false
		}

		return gridParam;
	}
	function formatCrAcceptance(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue + '\')">' + cellValue + '</button>';
	}

	function viewDetail(crNo){
		openDialogPopup("/inside/cr/acceptance/acceptancePopup"
				, {crNo: crNo}
				, "popupDialog", 'l', 710);
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridCrAcceptanceList"/>
</body>
</html>