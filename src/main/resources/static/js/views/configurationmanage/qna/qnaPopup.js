var popupGridParam;
var gridId='gridConfigQnaPopup';
var updateflag;
var files = [];

function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridConfigQnaPopup',
			formId : 'formConfigQnaPopup',
			url : '/configurationmanage/qna/selecQnaList',
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
	$('#listCount').text($('#gridConfigQnaPopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

function removeFile() {
	$('.appendFile').hide();
	$(".singleFileUpload").show();
	$("#fileDelete").val(true);
}

function fileUpload(){
	$('#qnaFile').click();
}

function fileChange(){
	var fileName = $('#qnaFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function fileDownload(qnaCd,fileNo,fileNm){
	console.log("pushed");
	$("form[name=tmpForm]")
	.attr("action","/configurationmanage/qna/fileDownload?qnaCd="+qnaCd+"&fileNo="+fileNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
}

function isValidation(){
	/*if($.trim($("#qnaType").val()) === ""){
		isValidDataEmpty("qnaType", "form.configType");
		return false;
	}*/
	if($.trim($("#qnaTitle").val()) === ""){
		isValidDataEmpty("qnaTitle", "form.title");
		return false;
	}
	if($.trim($("#contents").val()) === ""){
		isValidDataEmpty("contents", "form.contents");
		return false;
	}
	return true;
}


function updateQna(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#qnaFile")[0].files[0]);
	param.append('configParam', JSON.stringify($('#formConfigQnaPopup').serializeObject()));
	callAjaxUpload(param, "/configurationmanage/qna/updateQna", updateQnaCallback);
}

function updateQnaCallback(response) {
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

function isReplyValidation() {
	if($.trim($("#answerUserNm").val()) === ""){
		isValidDataEmpty("#answerUserNm", "form.answerUserNm");
		return false;
	}
	if($.trim($("#answer").val()) === ""){
		isValidDataEmpty("#answer", "form.answer");
		return false;
	}
	return true;
}

function saveReplyQna(){
	if(!isReplyValidation()){
		return;
	}
	var param = new FormData();
	param.append('replyParam', JSON.stringify($('#formConfigQnaPopup').serializeObject()));
	console.log(param);
	callAjaxUpload(param, "/configurationmanage/qna/replyQna", replyQnaCallback);
}

function replyQnaCallback(response) {
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
			closePopup('qnaPopup');
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}