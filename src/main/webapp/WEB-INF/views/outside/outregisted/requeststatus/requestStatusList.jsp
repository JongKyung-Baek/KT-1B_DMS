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
				gridId : 'gridOutregistedRequestStatusList',
				formId : 'formOutregistedRequestStatus',
				url : '/outside/outregisted/requeststatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function viewOutregDistributionStatus(requestNo){
		openDialogPopup("/outside/outregisted/requeststatus/requestStatusPopup", {requestNo : requestNo}, "popupDialog", 'l', 560, true, 'popup-common');
	}

	function deleteRequestNo(gridId) {
		var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
		var isSuccess=true;
		if(selrow.length <= 0){
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}
		
		var strUserCd = '${sessionUser.userCd}';
		
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			if(strUserCd != data.requestUserCd){
				isSuccess=false;
				alertMessage(g_msg('msg.requestUserCompare'), function(){		//요청건 배포자만 삭제가 가능합니다.
					$(this).dialog("close");
				});
				return false;
			}

			if("REQUEST" != data.statusCd){
				isSuccess=false;
				alertMessage(g_msg('msg.requestStatusCompare'), function(){   //요청 상태만 삭제가 가능합니다.
					$(this).dialog("close");
				});
				return false;
			}
		});
		
		if(isSuccess){
			confirmMessage(g_msg("msg.confirmDeleteRequest"), function(){    //요청건을 삭제하시겠습니까?
				$(this).dialog("close");
			
				$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
					var data = $("#" + gridId).jqGrid('getRowData', item);
					var param = {
							  requestNo: data.requestOrgNo
					}
					console.log(param);
					callAjax(param, '/outside/outregisted/requeststatus/deleteRequestNo', deleteRequestCallback);
				});
			});
		}
	}
	
	function deleteRequestCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.completeDelete'), function(){		//삭제되었습니다.
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.failure"));		//실패하였습니다.
		}
	}
</script>
</head>
<body>
	<custom:listTemplate gridId="gridOutregistedRequestStatusList"/>
</body>
</html>
