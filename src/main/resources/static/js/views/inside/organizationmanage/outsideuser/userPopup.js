$(function() {
});

function saveUser(){

	if(!isValidObjEmpty($("#popCompanyCd"), 'form.companyNm')) {
		return false;
	}

	if(!isValidObjEmpty($("#popUserNm"), 'form.userNm')) {
		return false;
	}

	var email = $("#formPopup #email").val();

	if(!isValidObjEmpty($("#formPopup #email"), 'form.email')) {
		return false;
	}

	if(!isValidEmail(email)){
		alertMessage(g_msg('msg.invalidEmail'));
		return false;
	}

	if("I" === $("#saveFlag").val() && !isValidObjEmpty($("#userPwd"), 'form.userPwd')) {
		return false;
	}

	var param = $("#formPopup").serializeObject();

	callAjax(param, '/inside/organizationmanage/outsideuser/saveUser', function(response){
		if(response.success) {
			alertMessage(g_msg('msg.completeSave'), function() {
				searchList(gridParam);
				closePopup2('popupDialog');
				$(this).dialog("close");
			});
		}
		else {
			alertMessage(g_msg('msg.failSave'), function() {
			});

		}
	})
}






function closePopup2(dialog){
	$('body').css({"overflow":"auto"});
	searchList(gridParam);
	popupGridParam = '';
	$('#'+dialog+'a').dialog('close');
}