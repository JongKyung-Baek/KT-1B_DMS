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
				gridId : 'gridProductionDistributionStatusList',
				formId : 'formProductionDistributionStatus',
				url : '/inside/production/distributionstatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false
		}

		return gridParam;
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionDistributionStatusList"/>
</body>
</html>