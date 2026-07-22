
function saveApproval(flag){

	if('R' === flag) {			//반려시 배포요청반려사유 반드시 입력
		if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectRequestDesc')) {
			return false;
		}
	}

	var param = $("#formPrintApprovalPopup").serializeObject();
	param.saveType = flag;

	callAjax(param, '/inside/distribution/saveApproval', function(){
		if('A'==flag) {
			alertMessage(g_msg('msg.completeApprove'), function(){			//승인되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else if('R'==flag){
			alertMessage(g_msg('msg.completeReject'), function(){			//반려되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}
	})
}