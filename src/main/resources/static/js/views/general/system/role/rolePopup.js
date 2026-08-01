$(function() {
	$("input[name=groupNm]").focus();
});

function rolePopupText(key, fallback) {
	return window.SdmsI18n && typeof window.SdmsI18n.t === 'function'
		? window.SdmsI18n.t(key, fallback)
		: fallback;
}

function savePopup() {
	var param = $('#formPopup').serializeObject();
	console.log(param);
	callAjax(param, '/general/system/role/saveRoleGroup', function(){
		alertMessage(rolePopupText('feature.system.role.message.groupSaved', '사용자등급 정보를 저장했습니다.'), function(){
			closePopup('popupDialog');
			$(this).dialog("close");
		});

		initWindow();
	})
}
