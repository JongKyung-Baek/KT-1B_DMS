<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>KT-1B DMS</title>
<script>
	function setGridParam(){
		gridParam = {
				gridId : 'gridProductionDisposalApprovalList',
				formId : 'formProductionDisposalApproval',
				url : '/general/production/disposalApproval/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	//생산기술자료 폐기 승인/반려 팝업
	function viewDisposalApproval(destroyRequestNo){
		openDialogPopup("/general/production/disposalApproval/disposalApprovalPopup", {destroyRequestNo : destroyRequestNo}, "popupDialog", 'm', 290);
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionDisposalApprovalList"/>
</body>
</html>