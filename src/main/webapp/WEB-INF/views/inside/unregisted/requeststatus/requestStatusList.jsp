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
				gridId : 'gridUnregistedRequestStatusList',
				formId : 'formUnregistedRequestStatus',
				url : '/inside/unregisted/requeststatus/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function viewNoregDistributionStatus(requestNo){
		openDialogPopup("/inside/unregisted/requeststatus/requestStatusPopup", {requestNo : requestNo}, "popupDialog", 'l', 700);
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
					callAjax(param, '/inside/unregisted/requeststatus/deleteRequestNo', deleteRequestCallback);
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
</script>
</head>
<body>
	<custom:listTemplate gridId="gridUnregistedRequestStatusList"/>
</body>
</html>
