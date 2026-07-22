<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!-- QnA 답변쓰기 팝업 -->
<script>
var popupGridParam;
$(function() {
	//settingGrid('${gridInfo }',replyPopupGridParam() , 'popupGridParam');
});


function replyPopupGridParam(){
	popupGridParam = {
			gridId : 'gridReplyQna',
			formId : 'formReplyQna',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}

function isValidation() {
	if($.trim($("#replyPopupDialog #qnaTitle").val()) === ""){
		isValidDataEmpty("replyPopupDialog #qnaTitle", "form.qnaTitle");
		return false;
	}
	if($.trim($("#replyPopupDialog #insertUserNm").val()) === ""){
		isValidDataEmpty("replyPopupDialog #insertUserNm", "form.insertUid");
		return false;
	}
	if($.trim($("#replyPopupDialog #contents").val()) === ""){
		isValidDataEmpty("replyPopupDialog #contents", "form.contents");
		return false;
	}

	return true;
}

function saveReplyQna(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('replyParam', JSON.stringify($('#formReplyQna').serializeObject()));
	console.log(param);
	callAjaxUpload(param, "/bbs/qna/replyQna", replyQnaCallback);
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
</script>
<style>
	textarea#contents{height:180px;}
</style>
<div class="dialogContent qnaReplyPopup drawingRegisterPopup popup-base popup-actions-center" id="replyPopupDialog">
	<div class="popupHero">
		<h2>Q&A 답변 등록</h2>
		<p>답변 내용을 입력해 등록해 주세요.</p>
	</div>
	<form id="formReplyQna" name="formReplyQna">
		<input id="parentQnaCd" name="parentQnaCd" value="${data.qnaCd}" type="hidden"/>
		<ul class="section popupCard popupFormGrid">
			<li class="full">
				<custom:popupInputText name="qnaTitle" label="form.qnaTitle" value="RE: ${data.title}" id="qnaTitle" />
			</li>
			<li class="full">
				<custom:popupInputText name="insertUserNm" label="form.insertUid"  value="${insertUserNm }" id="insertUserNm" />
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${contents}" id="contents" />
			</li>
		</ul>
	</form>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 등록 --%>
		<custom:popupButton function="saveReplyQna()" name="save" label="btnOk" id="save"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
