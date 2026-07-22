<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script>
var popupGridParam;
var gridAddId = 'gridAddQna';
var formAddId = 'formAddQna';
var files = [];
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});


function setPopupGridParam(){
	popupGridParam = {
			gridId : gridAddId,
			formId : formAddId,
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}
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

function saveAddQna(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('bbsParam', JSON.stringify($('#formAddQna').serializeObject()));
	console.log(JSON.stringify($('#formAddQna').serializeObject()));
	callAjaxUpload(param, "/bbs/qna/bbsQna", addQnaCallback);
}

function addQnaCallback(response) {
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
</script>
<style>
	textarea#contents{height:80px;}
</style>
<div class="dialogContent qnaAddPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>Q&A 등록 정보</h2>
		<p>필수 항목을 입력해 등록해 주세요.</p>
	</div>
	<form id="formAddQna" name="formAddQna">
		<ul class="section popupCard popupFormGrid">
			<li class="full">
				<custom:popupInputText name="qnaTitle" label="form.qnaTitle" value="${title}" id="qnaTitle" />
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
		<custom:popupButton function="saveAddQna()" name="save" label="btnAdd" id="save"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
