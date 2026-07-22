<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<style>
	.sbr .ibx li > label { width: auto; }
</style>
<script>

	function setGridParam(){
		gridParam = {
			gridId : 'gridOutsideDuanzongPdmList',
			formId : 'formOutsideDuanzongPdm',
			url : '/outside/duanzong/pdm/selectList',
			pagerId: 'gridListPager',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : true,
			numbering : true,
			selectRowAction : 'check'
		}

		return gridParam;
	}

</script>
</head>
<body>
	<custom:listTemplate gridId="gridOutsideDuanzongPdmList"/>
</body>
</html>