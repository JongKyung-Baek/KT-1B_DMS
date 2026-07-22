<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<style>
body { text-align: left; }
</style>
<script>
	var treeList = '${treeList}';
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/system/menu/outside/menuList.js"></script>
</head>
<body>
	<div id="tree"></div>
</body>
</html>