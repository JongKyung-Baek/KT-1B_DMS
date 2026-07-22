<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
var gridId = 'gridOldCrHistoryList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formOutsideOldCrHistory',
				url : '/outside/cr/oldHistory/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
		return gridParam;
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridOldCrHistoryList"/>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>