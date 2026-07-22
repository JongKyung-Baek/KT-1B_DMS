<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
	function setGridParam(){
		gridParam = {
				gridId : 'gridInsideOutregistedAcceptList',
				formId : 'formInsideOutregistedAccept',
				url : '/outside/outregisted/accept/selectList',
				pagerId: 'gridListPager',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}
		 
		return gridParam;
	}

	function formatOpenView(cellValue, options, rowdata, action){
		return '<a onclick="openFile(\'UNREG_DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \''+ rowdata["fileNo"] +'\')">'+cellValue+'</a>';
	}

	function viewOutregDistributionStatus(requestNo){
		openDialogPopup("/outside/outregisted/requeststatus/requestStatusPopup", {requestNo : requestNo}, "popupDialog", 'l', 560);
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

			if("Y" == data.vendorAcceptYn){
				isSuccess=false;
				alertMessage(g_msg('msg.isNoTransAccept'), function(){		//이미 자료접수된 대상은 선택할 수 없습니다
					$(this).dialog("close");
				});
				return false;
			}
		});
		
		if(isSuccess){
			confirmMessage(g_msg("msg.transAccept"), function(){     //자료접수 하시겠습니까?
				$(this).dialog("close");

				$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
					var data = $("#" + gridId).jqGrid('getRowData', item);
					var param = {
						requestNo: data.requestOrgNo
						//requestNo: data.requestNo
						
					}
					console.log(param);
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
	
	
</script>
</head>
<body>                          
	<custom:listTemplate gridId="gridInsideOutregistedAcceptList"/>
</body>
</html>