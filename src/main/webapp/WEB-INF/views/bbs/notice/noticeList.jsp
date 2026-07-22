<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var multiflag;
var user = '${sessionUser.roleGroup}';
if(user == 'RG_001' || user == 'RG_002'){
	multiflag = true;
}else{
	multiflag = false;
}
var gridId = 'gridBbsNoticeList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formBbsNotice',
				url : '/bbs/notice/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : multiflag,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function addNotice(){
		var popupHeight = Math.min($(window).height() - 100, 800);
		openDialogPopup("/bbs/notice/addPopup", {}, "popupDialog", 'm', popupHeight, true, 'popup-common');
	}

	function deleteNotice(){
		var list = [];
		var isSuccess=true;
		if($("#gridBbsNoticeList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#gridBbsNoticeList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridBbsNoticeList").jqGrid('getRowData', item);
			console.log(data.noticeCd);
			list.push({noticeCd:data.noticeCd});
		});
		var param = {list:list};
		console.log(param);
		callAjax( param, "/bbs/notice/deleteNotice", deleteNoticeCallback, 'json');
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
		var popupHeight = Math.min($(window).height() - 100, 810);
		openDialogPopup("/bbs/notice/noticePopup", {noticeCd:noticeCd}, "popupDialog", 'm', popupHeight, true, 'popup-common');
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridBbsNoticeList"/>
	</div>
</body>
</html>
