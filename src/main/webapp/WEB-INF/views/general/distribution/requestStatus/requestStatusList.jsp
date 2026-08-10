<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<sec:authentication property="principal" var="sessionUser" />
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/distribution/acceptance/common-form-vuexy.js"></script>
<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;

	function setGridParam(){
		gridParam = {
				gridId : 'gridRequestStatusList',
				formId : 'formRequestStatus',
				url : '/general/distribution/requeststatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		return '<a onclick="openDialog(\'' + cellValue + '\', \'' + rowdata["objectType"] + '\')">'+cellValue+'</a>';
	}

	function openDialog(requestNo, objectType){
		openDialogPopup("/general/distribution/requeststatus/requestStatusPopup", {requestNo: requestNo, objectType : objectType}, "popupDialog", 'xl', 525, true, 'popup-common popup-request-status');
	}
	
	function deleteRequestNo(gridId) {
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		var isSuccess=true;
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
		
var strUserCd = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${sessionUser.userCd}</spring:escapeBody>';
		
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			if(strUserCd != data.requestUserCd){
				isSuccess=false;
				alertMessage(g_msg('msg.requestUserCompare'), function(){		//��û�� �����ڸ� ������ �����մϴ�.
					$(this).dialog("close");
				});
				return false;
			}

			if("REQUEST" != data.statusCd){
				isSuccess=false;
				alertMessage(g_msg('msg.requestStatusCompare'), function(){   //��û ���¸� ������ �����մϴ�.
					$(this).dialog("close");
				});
				return false;
			}
		});
		
		if(isSuccess){
			confirmMessage(g_msg("msg.confirmDeleteRequest"), function(){    //��û���� �����Ͻðڽ��ϱ�?
				$(this).dialog("close");
			
				$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
					var data = $("#" + gridId).jqGrid('getRowData', item);
					var param = {
							  requestNo: data.requestOrgNo
					}
					console.log(param);
					callAjax(param, '/general/distribution/requeststatus/deleteRequestNo', deleteRequestCallback);
				});
			});
		}
	}
	
	function deleteRequestCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.completeDelete'), function(){		//�����Ǿ����ϴ�.
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.failure"));		//�����Ͽ����ϴ�.
		}
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridRequestStatusList"/>
	</div>
</body>
</html>
