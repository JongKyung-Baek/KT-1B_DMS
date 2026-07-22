function savePopup() {
	callAjax($('#formPopup').serializeObject(), '/inside/system/menu/saveMenu', function(){
		alertMessage(g_msg('msg.completeSave'), function(){			// 저장되었습니다.
			closePopup('popupDialog');
			$(this).dialog("close");
		});

		setTree();
	})
}