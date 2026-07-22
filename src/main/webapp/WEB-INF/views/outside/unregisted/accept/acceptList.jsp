<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.regex.*" %>
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
				gridId : 'gridOutsideUnregistedAcceptList',
				formId : 'formOutsideUnregistedAccept',
				url : '/outside/unregisted/accept/selectList',
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

	function viewNoregDistributionStatus(requestNo){
		openDialogPopup("/inside/unregisted/requeststatus/requestStatusPopup", {requestNo : requestNo}, "popupDialog", 'l', 700);
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
						// requestNo: data.requestOrgNo

						// 2023.07.03 (yskim) 요청번호 형식이 '<a onclick="viewNoregDistributionStatus(''20230703-N0001'')">20230703-N0001</a>'과 같이 되어 있음
						// 비활성화 하는 방향으로 수정해보겠음
						// Pattern pattern = Pattern.compile("''(.*?)''"); // 정규식 패턴 설정. '' 안에 있는 값을 추출합니다.
						// Matcher matcher = pattern.matcher(input);
						// if (matcher.find()) { // 패턴에 일치하는 값이 있으면
						// 	String extractedValue = matcher.group(1); // 첫번째 그룹(정규식 괄호 안)의 값을 추출합니다.
						// 	System.out.println(extractedValue); // 추출된 값을 출력합니다. 이 경우 '20230703-N0001'이 출력됩니다.
						// }
						requestNo: data.requestNo
						
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
	
	
</script>
</head>
<body>
	<custom:listTemplate gridId="gridOutsideUnregistedAcceptList"/>
</body>
</html>