var popupGridParam;
var gridId = 'gridCrRequestUploadPopup';
var emptyArray = [];
var files = [];
var drawingInfo;
var isInspection = false;

function isValidation(){
	//성명
	if($.trim($("#userNmPopup").val()) === ""){
		isValidDataEmpty("userNmPopup", "form.userNmChangePopup");
		return false;
	}
	
	if($("input[name=requestType]").val() == "I"){
		//비밀번호
		if($.trim($("#userPwd").val()) === ""){
			isValidDataEmpty("userPwd", "form.pwd");
			return false;
		}
		if(!checkPasswordRule($.trim($("#userPwd").val()))){
			alertMessage(g_msg('msg.pwdNotice'));
			return false;
		}
		if($.trim($("#userPwdConfirm").val()) === ""){
			isValidDataEmpty("userPwdConfirm", "form.pwdConfirm");
			return false;
		}
		if($.trim($("#userPwd").val()) !== $.trim($("#userPwdConfirm").val())){
			alertMessage(g_msg('msg.pwdCompare'));
			return false;
		}
		
		//email
		if($.trim($("#email").val()) === ""){
			isValidDataEmpty("email", "form.email");
			return false;
		}
	}

	//요청사유
	if($.trim($("#requestReason").val()) === ""){
		isValidDataEmpty("requestReason", "form.requestReason");
		return false;
	}
	return true;
}

function requestUser(){
	if(!isValidation()){
		return;
	}

	var param = $('#request').serializeObject();
	callAjax(param, "/outside/user/information/request", requestUserCallback);
}

/**
 * 사용자 승인요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestUserCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		var messageKey = response && response.message;
		alertMessage(messageKey ? g_msg(messageKey) : g_msg("msg.requestFailure"));
	}
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listCount').text($('#'+gridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}
