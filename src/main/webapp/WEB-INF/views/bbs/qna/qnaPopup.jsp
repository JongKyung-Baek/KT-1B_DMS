<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/qna/qnaPopup.js"></script>
<!-- QnA 팝업(QnA 상세보기) -->
<script>
var popupGridParam;
var gridId='gridQnaPopup';
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');

	$(".ui-dialog .ui-dialog-titlebar-close").click(function(event){
		console.log("close-popup");
		closePopup('popupDialog');
	});
});

function isValidation(){
	if($.trim($("#qnaTitle").val()) === ""){
		isValidDataEmpty("qnaTitle", "form.qnaTitle");
		return false;
	}
	if($.trim($("#insertUserNm").val()) === ""){
		isValidDataEmpty("insertUserNm", "form.insertUserNm");
		return false;
	}
	if($.trim($("#contents").val()) === ""){
		isValidDataEmpty("contents", "form.contents");
		return false;
	}
	return true;
}

function replyQna(qnaCd){
	var popupHeight = Math.min($(window).height() - 140, 600);
	openDialogPopup("/bbs/qna/replyPopup", { "qnaCd" : qnaCd}, "popupDialog", 'm', popupHeight, true, 'popup-common');
}

function addQnaCallback(response) {
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialogQna');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}
</script>
<style>
	textarea#contents{height:80px;}
</style>
<div class="dialogContent qnaDetailPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>Q&A 상세 정보</h2>
		<p>문의 내용을 확인하고 답변을 등록해 주세요.</p>
	</div>
	<form id="formQnaPopup">
		<input type="hidden" name="parentQnaCd" id="parentQnaCd" value="${data.parentQnaCd}">
		<input type="hidden" name="qnaCd" id="qnaCd" value="${data.qnaCd}">
		<input type="hidden" name="hitCount" id="hitCount" value="${hitCount}">
		<ul class="section popupCard popupFormGrid">
			<li class="full">
				<custom:popupInputText name="qnaTitle" label="form.qnaTitle" value="${data.title }" id="qnaTitle" disabled="disabled"/>
			</li>
			<li class="full">
				<custom:popupInputText name="insertUserNm" label="form.insertUid" value="${data.insertUserNm }" id="insertUserNm" disabled="disabled"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${data.contents }" id="contents" disabled="Y"/>
			</li>
		</ul>
	</form>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<!--<%-- 수정 --%>
		 <c:if test="${(not empty data.parentQnaCd && sessionUser.userId eq 'admin') || (empty data.parentQnaCd &&'admin' ne sessionUser.userId)}">
			<button class="ui-button ui-corner-all bottomBtn" onclick="updateQna()"><spring:message code="btnUpdate"/></button>
		</c:if>  -->
		<c:if test="${sessionUser.roleGroup eq 'RG_001' && empty data.parentQnaCd}">
			<custom:popupButton function="replyQna('${data.qnaCd }')" name="save" label="btnReply" id="save"/>
		</c:if>
		<custom:popupButton function="closePopup('qnaPopup')" name="close" label="btn.close" id="close"/>
	</div>
</div>
