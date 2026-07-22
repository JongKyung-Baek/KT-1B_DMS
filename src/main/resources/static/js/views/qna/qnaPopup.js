function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridQnaPopup',
			formId : 'formQnaPopup',
			url : '/bbs/qna/selecQnaList',
			pagerId : '',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
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
	$('#listCount').text($('#gridQnaPopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}
