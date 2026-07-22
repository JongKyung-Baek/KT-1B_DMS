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
				gridId : 'gridInsideOutregistedStatusList',
				formId : 'formInsideOutregistedStatus',
				url : '/inside/outregisted/status/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function formatOpenView(cellValue, options, rowdata, action){
		return '<a onclick="openFile(\'UNREG_DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \''+ rowdata["fileNo"] +'\')">'+cellValue+'</a>';
	}

</script>
</head>
<body>
	<custom:listTemplate gridId="gridInsideOutregistedStatusList"/>
</body>
</html>