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
if(user == 'RG_001' || user == 'RG_002'){
	multiflag = true;
}else{
	multiflag = false;
}
var gridId = 'gridConfigNoticeList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formConfigNotice',
				url : '/configurationmanage/notice/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : multiflag,
				numbering : true,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function addNotice(){
		openDialogPopup("/configurationmanage/notice/addPopup", {}, "popupDialog", 'm', 0, true, 'popup-common');
	}

	function deleteNotice(){
		var list = [];
		var isSuccess=true;
		if($("#gridConfigNoticeList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#gridConfigNoticeList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridConfigNoticeList").jqGrid('getRowData', item);
			console.log(data.noticeCd);
			list.push({noticeCd:data.noticeCd});
		});
		var param = {list:list};
		console.log(param);
		callAjax( param, "/configurationmanage/notice/deleteNotice", deleteNoticeCallback, 'json');
	}

	function deleteNoticeCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}

	function formatViewNotice(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + rowdata['noticeCd'] +'\')">' + cellValue + '</href>';
	}
	function viewDetail(noticeCd){
		var popupHeight = Math.min($(window).height() - 140, 750);
		openDialogPopup("/configurationmanage/notice/noticePopup", {noticeCd:noticeCd}, "popupDialog", 'm', popupHeight, true, 'popup-common');
	}

</script>
</head>
<body>
	<custom:listTemplate gridId="gridConfigNoticeList"/>
</body>
</html>
