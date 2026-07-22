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
				gridId : 'gridProductionDisposalList',
				formId : 'formProductionDisposal',
				url : '/inside/production/disposal/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function destroy(){
		if($("#"+gridParam.gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else {
			openDialogPopup("/inside/production/disposal/disposalRequestPopup", {}, "popupDialog", 's', 310);
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridProductionDisposalList"/>
</body>
</html>
