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
var multiflag;
var user = '${sessionUser.roleGroup}';
if(user == 'RG_001'){
	multiflag = true;
}else{
	multiflag = false;
}
	function setGridParam(){
		gridParam = {
				gridId :'gridConfigQnaList',
				formId : 'formConfigQna',
				url : '/configurationmanage/qna/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : multiflag,
				numbering : true,
			 	selectRowAction : 'check',
		}

		return gridParam;
	}

	function addQna(){
		var popupHeight = Math.min($(window).height() - 60, 880);
		openDialogPopup("/configurationmanage/qna/addPopup", {}, "popupDialog", 'm', popupHeight, true, 'popup-common');
	}

	function deleteQna(){
		var list = [];
		var isSuccess=true;
		if($("#gridConfigQnaList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#gridConfigQnaList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridConfigQnaList").jqGrid('getRowData', item);
			console.log(data);

			list.push({qnaCd:data.qnaCd,parentQnaCd:data.parentQnaCd});
			for(var i = 0; i < list.length; i++){
				var addData = $("#gridConfigQnaList").jqGrid('getRowData', ++item);
				if(data.parentQnaCd == "" && data.qnaCd == addData.parentQnaCd){
					list.push({qnaCd:addData.qnaCd,parentQnaCd:addData.parentQnaCd});
				}
			}
		});
		var param = {list:list};
		console.log(param);
		callAjax( param, "/configurationmanage/qna/deleteQna", deleteQnaCallback, 'json');
	}

	function deleteQnaCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}

	function viewDetail(qnaCd){
		var popupHeight = Math.min($(window).height() - 50, 900);
		openDialogPopup("/configurationmanage/qna/qnaPopup", {qnaCd:qnaCd}, "qnaPopup", 'm', popupHeight, true, 'popup-common');
	}

	function formatViewQna(cellValue, options, rowdata, action){

		if(rowdata['parentQnaCd'] > 0 ){
			return '<a href="javascript: viewDetail(\'' + rowdata['qnaCd'] +'\')" style="text-indent: 3em;display:block;">' + cellValue + '</href>';
		} else{
		return '<a href="javascript: viewDetail(\'' + rowdata['qnaCd'] +'\')">' + cellValue + '</href>';
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridConfigQnaList"/>
	<div id="qnaPopup" class="dialogContainer"></div>
</body>
</html>
