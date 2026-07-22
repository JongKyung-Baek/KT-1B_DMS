<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;

		function openDialog() {
			openDialogPopup("/outside/sw/request/requestPopup",{}, "popupDialog", 'xl', 740, true, 'popup-common popup-request');
		}

	function setGridParam(){
		gridParam = {
				gridId : 'gridOutsideSwRequestList',
				formId : 'formOutsideSwRequest',
				url : '/outside/sw/request/selectList',
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

	function formatRequestStatus(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue + '\')">' + cellValue + '</button>';
	}

	function viewDetail(requestNo){
		openDialogPopup("/outside/sw/request/requestStatusPopup"
				, {requestNo: requestNo}
				, "popupDialog", 'xl', 555, true, 'popup-common popup-request-status');
	}
	
	function vendorAccept(gridId) {
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		var isSuccess=true;
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
			
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			
			if("APPROVAL" != data.statusCd){
				isSuccess=false;
				alertMessage(g_msg('msg.isOkApproval'), function(){		    //승인 상태만 선택 가능합니다.
					$(this).dialog("close");
				});
				return false;
			}
			
			if("Y" == data.vendorAcceptYn){
				isSuccess=false;
				alertMessage(g_msg('msg.isNoVendorAccept'), function(){		//이미 배포접수된 대상은 선택할 수 없습니다
					$(this).dialog("close");
				});
				return false;
			}
		});
		
		if(isSuccess){
			confirmMessage(g_msg("msg.distributionAccept"), function(){     //배포접수 하시겠습니까?
				$(this).dialog("close");

				$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
					var data = $("#" + gridId).jqGrid('getRowData', item);
					var param = {
						requestNo: data.requestOrgNo
					}
					//console.log(param);
					callAjax(param, '/outside/commonRequest/vendorAccept', vendorAcceptCallback);
				});
			});
		}
	}
	
	function vendorAcceptCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.acceptanceSuccess'), function(){		//접수가 완료되었습니다.
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.acceptanceFailure"));				//접수가 실패했습니다.
		}
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
	
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridOutsideSwRequestList"/>
	</div>
</body>
</html>
