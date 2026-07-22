var approvalFlag;
/**
 * 접수자 정보 리스트
 * @returns
 */
function setPopupRequestInfoGridParam(){
	popupRequestInfoGridParam = {
			gridId : 'gridRequestInfoPopup',
			formId : 'formApprovalInfoPopup',
			url : '/inside/production/approval/selectRequestUserList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}
	return popupRequestInfoGridParam;
}

/**
 * 배포승인 자료 목록
 * @returns
 */
function setPopupObjectInfoGridParam(){
	popupObjectInfoGridParam = {
			gridId : 'gridObjectInfoPopup',
			formId : 'formApprovalInfoPopup',
			url : '/inside/production/approval/selectObjectList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}
	return popupObjectInfoGridParam;
}


/**
 * 생산기술자료 배포 반려 시 필수 입력값 체크
 * @returns
 */
function validate(){
	if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectDesc')) return false;
	return true;
}


/**
 * 생산기술자료 배포 승인/반려
 * @returns
 */
function approval(flag){
	approvalFlag = flag;
	//반려시
	if('R' === approvalFlag) {
		if(!validate()) {
			return false;
		}
	}
	var param = $("#formApprovalInfoPopup").serializeObject();

	var aJsonArray = new Array();
	$.each($("#gridRequestInfoPopup").getRowData(), function(index, item){
		aJsonArray.push(item);
	});
	param.approvalPopupList = aJsonArray;

	aJsonArray = new Array();
	$.each($("#gridObjectInfoPopup").getRowData(), function(index, item){
		aJsonArray.push(item);
	});
	param.list = aJsonArray;
	param.requestPurpose = 'NEW';
	param.saveType = approvalFlag;
	callAjax(param, '/inside/production/approval/approval', approvalCallback);
}


/**
 * 승인/반려 후 결과 메시지 출력
 * @param response
 * @returns
 */
function approvalCallback(response){
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
	}else if(response.failReason === 'destroyInProgress'){
		alertMessage(g_msg("msg.destroyInProgress", response.data));
	}else{
		alertMessage(g_msg("msg.error"));					//에러가 발생하였습니다.
	}
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listAcceptCount').text($('#gridRequestInfoPopup').jqGrid('getGridParam', 'reccount'));
	$('#listCount').text($('#gridObjectInfoPopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}