$(function() {
	$("input[name=groupNm]").focus();
});

function savePopup() {
	var param = $('#formPopup').serializeObject();
	console.log(param);
	callAjax(param, '/inside/system/role/saveRoleGroup', function(){
		alertMessage(g_msg('msg.completeSave'), function(){			// 저장되었습니다.
			closePopup('popupDialog');
			$(this).dialog("close");
		});

		initWindow();
	})
}