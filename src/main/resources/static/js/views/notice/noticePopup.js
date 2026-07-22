function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridNoticePopup',
			formId : 'formNoticePopup',
			url : '/bbs/notice/selecNoticeList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listCount').text($('#gridNoticePopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

function removeFile() {
	$('.appendFile').hide();
	$(".singleFileUpload").show();
}

function fileChange(){
	var fileName = $('#noticeFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function updateNotice(){
	//var param = $('#formNoticePopup').serializeObject();
	//callAjax(param, '/bbs/notice/updateNotice', updateNoticeCallback);
	var param = new FormData();
	param.append('file', $("#noticeFile")[0].files[0]);
	param.append('bbsParam', JSON.stringify($('#formNoticePopup').serializeObject()));
	callAjaxUpload(param, "/bbs/notice/updateNotice", updateNoticeCallback);
}

function updateNoticeCallback(response) {
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}

//function saveAddNotice(){
//	if(!isValidation()){
//		return;
//	}
//	var param = new FormData();
//	param.append('file', $("#noticeFile")[0].files[0]);
//	param.append('bbsParam', JSON.stringify($('#formAddNotice').serializeObject()));
//	callAjaxUpload(param, "/bbs/notice/bbsNotice", addNoticeCallback);
//}
//
//function addNoticeCallback(response) {
//	if(response.success){
//		infoMessage(g_msg('msg.requestComplete'), function(){
//			searchList(gridParam);
//			closePopup('popupDialog');
//			$(this).dialog("close");
//		});
//	}else{
//		alertMessage(g_msg("msg.requestFailure"));
//	}
//}
