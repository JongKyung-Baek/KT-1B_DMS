<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
var gridId = 'gridProductionStatusList';
var formId = 'formProductionStatus';

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/production/productionstatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	//배포 승인요청
	function request(){
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
		openDialogPopup('/inside/production/productionstatus/requestPopup', {}, 'popupDialog', 'xl', '600');
	}

	function formatObjectNo(cellValue, options, rowdata, action){
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] + '\')">' + cellValue + '</a>';
	}




	function viewDetail(objectNo, objectType){
		openDialogPopup("/inside/production/productionstatus/distributionHistoryPopup", {objectNo: objectNo, objectType: objectType}, "popupDialog", 'l', 535);
	}


</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionStatusList"/>
</body>
</html>