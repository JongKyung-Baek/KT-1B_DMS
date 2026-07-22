var approvalFlag;
/**
 * 생산기술 자료 반려시 필수 값 체크
 * @returns
 */
function validate(){
	if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectDesc')) return false;
	return true;
}

/**
 * 생산기술 자료 페기 승인 요청
 * @returns
 */
function disposalApproval(flag){
	approvalFlag = flag;
	//반려시
	if('R' === approvalFlag) {
		if(!validate()) {
			return false;
		}
	}

	var param = $("#formDisposalApprovalPopup").serializeObject();
	param.saveType = approvalFlag;
	callAjax(param, '/inside/production/disposalApproval/destroyApproval', disposalApprovalCallback);
}


/**
 * 요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function disposalApprovalCallback(response){
	var msgCode;
	if(response.success){
		if('A' == approvalFlag){
			msgCode = 'msg.completeApprove';		//승인되었습니다.
		}else if('R' == approvalFlag){
			msgCode = 'msg.completeReject';			//반려되었습니다.
		}
		infoMessage(g_msg(msgCode), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.error"));					//에러가 발생하였습니다.
	}
}


function formatOpenView(cellValue, options, rowdata, action){

	return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\')">'+cellValue+'</a>';
}
