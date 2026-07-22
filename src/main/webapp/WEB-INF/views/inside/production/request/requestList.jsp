<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
var gridId = 'gridProductionRequestList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formProductionRequest',
				url : '/inside/production/request/selectList',
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
		var popupHeight = Math.min(window.innerHeight - 80, 660);
		openDialogPopup('/inside/production/request/requestPopup', {}, 'popupDialog', 'l', popupHeight, true, 'popup-common popup-request');
	}

	//출력 승인요청
	function printRequest(){
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
		var popupHeight = Math.min(window.innerHeight - 100, 600);
		openDialogPopup('/inside/production/request/printRequestPopup', {}, 'popupDialog', 's', popupHeight);
	}

	function searchAll(){
		openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'PRODUCTION'}, "searchAllPopup", 's', 600);
	}

	function formatViewFile(cellValue, options, rowdata, action){
		if(undefined == cellValue) {
			return '';
		}else{
			return '<a onclick="openFile(\'PRODUCT\', \''+rowdata["objectType"]+'\', null, \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\')">'+cellValue+'</a>';
		}
	}

	function changeObjectClass(){
		var param = {
				objectType : $('#objectType_select').val()
				};
		callAjax(param, "/inside/production/request/selectObjectClassCd", setObjectClassCd);
	}


	function setObjectClassCd(response){
		$("#objectClassCd2_select").empty().trigger('change');
		var newOption;
		newOption = new Option(g_msg('msg.selectItem'), '', false, false);
		$.each(response, function(index, data){
			newOption = new Option(data.comboLabel, data.comboVal, false, false);
			$("#objectClassCd2_select").select2({minimumResultsForSearch: 1});
			$("#objectClassCd2_select").append(newOption).trigger('change');
		});
	}


</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionRequestList"/>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
