var popupGridParam;
var updateflag;
var files = [];

function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridConfigNoticePopup',
			formId : 'formConfigNoticePopup',
			url : '/configurationmanage/notice/selecNoticeList',
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
	$('#listCount').text($('#gridConfigNoticePopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

function removeFile() {
	$('.appendFile').hide();
	$(".singleFileUpload").show();
	$("#fileDelete").val(true);
}

function fileUpload(){
	$('#noticeFile').click();
}

function fileChange(){
	var fileName = $('#noticeFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function fileDownload(noticeCd,fileNo,fileNm){
	console.log("pushed");
	$("form[name=tmpForm]")
	.attr("action","/configurationmanage/notice/fileDownload?noticeCd="+noticeCd+"&fileNo="+fileNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
}

function isValidation(){
	if($.trim($("#noticeTitle").val()) === ""){
		isValidDataEmpty("noticeTitle", "form.title");
		return false;
	}
	if($.trim($("#contents").val()) === ""){
		isValidDataEmpty("contents", "form.contents");
		return false;
	}
	return true;
}

function updateNotice(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#noticeFile")[0].files[0]);
	param.append('configParam', JSON.stringify($('#formConfigNoticePopup').serializeObject()));
	callAjaxUpload(param, "/configurationmanage/notice/updateNotice", updateNoticeCallback);
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
