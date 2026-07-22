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
if(user == 'RG_001'){
	multiflag = true;
}else{
	multiflag = false;
}
	function setGridParam(){
		gridParam = {
				gridId :'gridBbsQnaList',
				formId : 'formBbsQna',
				url : '/bbs/qna/selectList',
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

	function addQna(){
		var popupHeight = Math.min($(window).height() - 100, 600);
		openDialogPopup("/bbs/qna/addPopup", {}, "popupDialog", 'm', popupHeight, true, 'popup-common');
	}

	function deleteQna(){
		var list = [];
		var isSuccess=true;
		if($("#gridBbsQnaList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#gridBbsQnaList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridBbsQnaList").jqGrid('getRowData', item);
			console.log(data);

			list.push({qnaCd:data.qnaCd,parentQnaCd:data.parentQnaCd});
			for(var i = 0; i < list.length; i++){
				var addData = $("#gridBbsQnaList").jqGrid('getRowData', ++item);
				if(data.parentQnaCd == "" && data.qnaCd == addData.parentQnaCd){
					list.push({qnaCd:addData.qnaCd,parentQnaCd:addData.parentQnaCd});
				}
			}
		});
		var param = {list:list};
		console.log(param);
		callAjax( param, "/bbs/qna/deleteQna", deleteQnaCallback, 'json');
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
		var popupHeight = Math.min($(window).height() - 100, 600);
		openDialogPopup("/bbs/qna/qnaPopup", {qnaCd:qnaCd}, "qnaPopup", 'm', popupHeight, true, 'popup-common');
	}

	function formatViewQna(cellValue, options, rowdata, action){

		if(rowdata['parentQnaCd'] > 0 ){
			return '<a href="javascript: viewDetail(\'' + rowdata['qnaCd'] +'\')" style="text-indent: 3em;display:block;">' + cellValue + '</href>';
		} else{
		return '<a href="javascript: viewDetail(\'' + rowdata['qnaCd'] +'\')">' + cellValue + '</href>';
		}
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridBbsQnaList"/>
	</div>
	<div id="qnaPopup" class="dialogContainer"></div>
</body>
</html>
