<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>

	function addDuanzongfdms() {
		openDialogPopup("/outside/duanzong/fdms/docsAddPopup", {}, "popupDialog", 'm', 500);
	}

	function formatRequestStatus(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue + '\')">' + cellValue + '</button>';
	}

	function viewDetail(managementNo){
		openDialogPopup("/outside/duanzong/fdms/docsStatusPopup", {managementNo: managementNo}, "popupDialog", 'm', 390);
	}

	function setGridParam(){
		gridParam = {
			gridId : 'gridOutsideDuanzongDocsList',
			formId : 'formOutsideDuanzongDocs',
			url : '/outside/duanzong/fdms/selectList',
			pagerId: 'gridListPager',
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
	<custom:listTemplate gridId="gridOutsideDuanzongDocsList"/>
</body>
</html>