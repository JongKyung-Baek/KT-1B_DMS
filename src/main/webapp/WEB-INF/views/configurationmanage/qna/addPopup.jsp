<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script>
var popupGridParam;
var gridAddId = 'gridAddConfigQna';
var formAddId = 'formAddConfigQna';
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
	/* if($.trim($("#qnaType").val()) === ""){
		isValidDataEmpty("qnaType", "form.configType");
		return false;
	}
	if($.trim($("#companyNm").val()) === ""){
		isValidDataEmpty("companyNm", "form.companyNm");
		return false;
	}
	if($.trim($("#insertUserNm").val()) === ""){
		isValidDataEmpty("insertUserNm", "form.insertUserNm");
		return false;
	} */
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
var param = $(formAddId).serializeObject();
console.log(param);

function fileUpload(){
	$('#qnaFile').click();
}

function fileChange(){
	var fileName = $('#qnaFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function saveAddQna(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#qnaFile")[0].files[0]);
	param.append('configParam', JSON.stringify($('#formAddConfigQna').serializeObject()));
	callAjaxUpload(param, "/configurationmanage/qna/configQna", addQnaCallback);
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
	textarea#contents{height:180px;}
</style>
<div class="dialogContent qnaAddPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>Q&A 등록 정보</h2>
		<p>필수 항목을 입력해 등록해 주세요.</p>
	</div>
	<form id="formAddConfigQna" name="formAddConfigQna">
		<ul class="section popupCard popupFormGrid">
			<li class="half">
				<custom:popupSelectBox options="${qnaType}" name="qnaType" label="form.configType" id="qnaType"/>
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUserNm"  value="${sessionUser.userNm}" id="insertUserNm" readOnly="readOnly" />
			</li>
			<li class="full">
				<custom:popupInputText name="companyNm" label="form.companyNm"  value="${sessionUser.companyNm}" id="companyNm" readOnly="readOnly" />
			</li>
			<li class="full">
				<custom:popupInputText name="title" label="form.title" value="${title}" id="qnaTitle" />
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${contents}" id="contents" />
			</li>
		</ul>
	</form>
	<ul class="section popupCard popupFormGrid uploadOnly">
		<li class="singleFileUpload full">
			<%--  파일추가 --%>
			<label><spring:message code="grid.appendFile"/></label>
			<div class="uploadLine">
				<form id="fileForm" name="fileForm" enctype="multipart/form-data">
					<input type="file" id="qnaFile" name="qnaFile" onchange="fileChange()" style="display: none;"/>
				</form>
				<input type="text" name="fileName" id="fileName" readonly="readonly"/>
				<button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
			</div>
		</li>
	</ul>
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
