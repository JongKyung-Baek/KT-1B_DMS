var popupGridId = 'gridPrintApprovalPopup';

function setPopupGridParam(){
	popupGridParam = {
			gridId : popupGridId,
			formId : 'formPrintApprovalPopup',
			url : '/inside/production/approval/selectPrintApprovalrList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}
	return popupGridParam;
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	//리스트 갯수
	var $grid = $('#'+popupGridId);
	$('#listCount').html($grid.getGridParam('records'));
	dialogToolbarWidth()
}

function approval(flag){
	approvalFlag = flag;
	//반려시
	if('R' === approvalFlag) {
		if(!validate()) {
			return false;
		}
	}
	var param = $("#formPrintApprovalPopup").serializeObject();
	param.saveType = flag;
	console.log(param);
	callAjax(param, '/inside/production/approval/printApproval', function(response){
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
	});
}


/**
 * 생산기술자료 배포 반려 시 필수 입력값 체크
 * @returns
 */
function validate(){
	if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectDesc')) return false;
	return true;
}
