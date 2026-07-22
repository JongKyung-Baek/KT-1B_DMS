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
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var gridId = 'gridDistributionHistoryList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formDistributionHistory',
				url : '/inside/distribution/history/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice'
		}
		return gridParam;
	}

	function stopDeal(){
		openDialogPopup("/inside/distribution/history/stopDealPopup", {}, "popupDialog", 'm', 650, true, 'popup-common');
	}

	function destroy(){
		var isSuccess=true;
		var destroyType='';
		//폐기중인 것만 선택가능하도록

		if($("#gridDistributionHistoryList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}

	    $.each($("#gridDistributionHistoryList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridDistributionHistoryList").jqGrid('getRowData', item);
			/* if("1" != data.destroyStatusCd){
				isSuccess=false;
				alertMessage(g_msg('msg.isOkUnderDestroy'), function(){			//폐기중 상태인 항목만 선택 가능합니다.
					$(this).dialog("close");
				});
				return false;
			}
			if(''===destroyType){
				destroyType = data.destroyType;
			}else if(destroyType != data.destroyType){
				isSuccess=false;
				alertMessage(g_msg('msg.differentDestroyType'), function(){			//서로 다른 폐기 유형을 선택할 수 없습니다.
					$(this).dialog("close");
				});
				return false;
			} */
		});
		if(isSuccess){
			openDialogPopup("/inside/distribution/history/destroyPopup", {destroyType: destroyType}, "popupDialog", 'm', 700, true, 'popup-common');
		}
	}
	
	function underDestroy(gridId) {
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		var isSuccess=true;
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
		
		var strUserCd = '${sessionUser.userCd}';
		
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			businessAreaCd = data.businessAreaCd;
			if("1" == data.destroyStatusCd || "2" == data.destroyStatusCd || "3" == data.destroyStatusCd  ) {
				isSuccess=false;
				alertMessage(g_msg('msg.isNoUnderDestroy'), function(){		//폐기중 대상이 아닙니다. 확인 바랍니다
					$(this).dialog("close");
				});
				return false;
			}
			
			if(strUserCd != data.purchaserUserCd){
				isSuccess=false;
				alertMessage(g_msg('msg.purchaserCompare'), function(){		//해당 구매담당자만 폐기중 변경이 가능합니다.
					$(this).dialog("close");
				});
				return false;
			}
		});
		
		if(isSuccess){
			confirmMessage(g_msg("msg.confirmUnderDestroy"), function(){ //폐기중으로 변경 하시겠습니까?
				$(this).dialog("close");
			
				$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
					var data = $("#" + gridId).jqGrid('getRowData', item);
					var param = {
							  requestNo: data.requestNo
							, objectId: data.objectId
							, fileNo: data.fileNo
					}
					//console.log(param);
					callAjax(param, '/inside/distribution/history/underDestroy', underDestroyCallback);
				});
			});
		}
	}
	
	function underDestroyCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){		//요청이 완료되었습니다.
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));					//요청이 실패했습니다
		}
	}
	
	function searchAll(){
		openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'HISTORY'}, "searchAllPopup", 'm', 600);
	}

	function formatOpenView(cellValue, options, rowdata, action){

		return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridDistributionHistoryList"/>
	</div>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
