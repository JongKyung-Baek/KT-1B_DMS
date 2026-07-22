<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 생산기술자료 접수자조회 탭팝업 리스트 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>tab popup - CollabHub</title>
<script>
	function openDialog() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"tablistpopup"}, "popupDialog", 'l', 760);
	}
</script>
<script>
	$(document).ready(function(){		
	});
</script>
<style>
</style>
</head>
<body>
	<button class="ui-button ui-corner-all" onclick="openDialog()">생산기술자료 접수자조회 탭 팝업 열기</button>
	<div id="popupDialog" class="dialogContainer"></div> <!-- 생산기술자료 접수자조회 탭팝업 버튼 -->
</body>
</html>
