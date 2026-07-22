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
				gridId : 'gridUnregistedRequestList',
				formId : 'formUnregistedRequest',
				url : '/inside/unregisted/request/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
		return gridParam;
	}

	function upload(){
		openDialogPopup("/inside/unregisted/request/registerPopup", {}, "popupDialog", 's', 240);
	}

	function requestDistribute(){
		var objectType;
		var businessAreaCd;
		if($("#"+gridParam.gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else{
			$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
				objectType = data.objectType;
				businessAreaCd = data.businessAreaCd;
				return false;
			});
			openDialogPopup("/inside/unregisted/request/distributionRequestPopup", {objectType: objectType, businessAreaCd : businessAreaCd}, "popupDialog", 'm', 490);
		}
	}

	function formatOpenView(cellValue, options, rowdata, action){
		return '<a onclick="openFile(\'UNREG\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\')">'+cellValue+'</a>';
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridUnregistedRequestList"/>
</body>
</html>
