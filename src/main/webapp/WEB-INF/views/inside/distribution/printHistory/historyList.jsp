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
				gridId : 'gridPrintHistoryList',
				formId : 'formPrintHistory',
				url : '/inside/distribution/printHistory/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

	function openDialog(){
		var isSuccess=true;
		var protectYn = "N";
		var productProtectYn= "N";
		var developmentProtectYn= "N";
		if($("#gridPrintHistoryList").getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
				$(this).dialog("close");
			});
			return false;
		}else {
			$.each($("#gridPrintHistoryList").getGridParam('selarrrow'), function(index, item){
				var data = $("#gridPrintHistoryList").jqGrid('getRowData', item);
				if("SUBMIT" == data.requestPurpose){
					isSuccess = false;
					alertMessage(g_msg('msg.noDisposalRequestSubmit'), function(){			//대관제출용은 폐기할 수 없습니다.
						$(this).dialog("close");
					});
				}

				if("Y" === data.protectYn){
					protectYn = "Y";
				}
				if( ("Development"===data.businessTypeCd) && ("Y" === data.protectYn) ){		//개발
					developmentProtectYn = "Y";
				}else if( ("Production"===data.businessTypeCd) && ("Y" === data.protectYn) ){	//양산
					productProtectYn = "Y";
				}

// 				if("Y"===data.protectYn){
// 					protectYn = "Y";
// 					return false;
// 				}

			});
			if(isSuccess){
				openDialogPopup("/inside/distribution/printHistory/disposalRequestPopup", {protectYn: protectYn, developmentProtectYn: developmentProtectYn, productProtectYn : productProtectYn}, "popupDialog", 'm', 550, true, 'popup-common');
			}
		}
	}

	function formatViewFile(cellValue, options, rowdata, action){
		if(undefined === cellValue){
			return '';
		}else{
			return '<a onclick="openFile(\'DISTRIBUTION\', \'' + rowdata["objectType"] + '\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\', \'' + rowdata["requestUserCd"] + '\')">'+cellValue+'</a>';
		}
	}

</script>
</head>
<body>
	<custom:listTemplate gridId="gridPrintHistoryList"/>

</body>
</html>
