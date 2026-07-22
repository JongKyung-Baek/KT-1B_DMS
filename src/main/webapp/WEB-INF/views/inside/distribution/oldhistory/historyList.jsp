<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
var gridId = '${gridId}';
var formId = '${formId}';

var url = '/inside/distribution/oldhistory/selectList';
if('${sessionUser.authSite}' == 'E'){
	url = '/outside/distribution/oldhistory/selectList';
}

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : url,
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
		return gridParam;
	}

	function destroy() {
		if($("#"+gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//���õ� �����Ͱ� �����ϴ�.
				$(this).dialog("close");
			});
		}else{
			var aJsonArray = new Array();
			
			$.each($("#"+gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#"+gridId).jqGrid('getRowData', item);

				console.log(data);

				if('DESTROY' !== data.taskState) {
					var aJson = new Object();
					aJson.rowDataId = data.rowDataId;
					aJsonArray.push(aJson);
				}
			});

			if(0 >= aJsonArray.length) {
				alertMessage(g_msg('msg.errorAlreadyDestory'));
				return false;
			}

			try { console.log(aJsonArray); } catch(e) { }

			confirmMessage(g_msg("msg.confirmDestory"), function(){
				$(this).dialog("close");

				var param = {list: aJsonArray};

				callAjax(param, "/inside/distribution/oldhistory/destroyOldHistory", function(response){
					if(response.success) {
						alertMessage(g_msg("msg.completeDestory"));
						searchList(gridParam);
					}
					else {
						alertMessage(g_msg("msg.failure"));
						console.log(response.failReason);
					}
				}, 'json', false);
			});
			
			//openDialogPopup("/common/viewer/openViewerPopup", {list:JSON.stringify(aJsonArray), requestType : requestType, objectType : objectType}, "viewerDialog", 's', 500);
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="${gridId }"/>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>