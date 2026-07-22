<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script>
var popupGridParam;
var gridAddId = 'gridAddConfigNotice';
var formAddId = 'formAddConfigNotice';
var files = [];
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});


function setPopupGridParam(){
	popupGridParam = {
			gridId : gridAddId,
			formId : formAddId,
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}
	return popupGridParam;
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

function fileUpload(){
	$('#noticeFile').click();
}

function fileChange(){
	var fileName = $('#noticeFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function saveAddNotice(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#noticeFile")[0].files[0]);
	param.append('configParam', JSON.stringify($('#formAddConfigNotice').serializeObject()));
	callAjaxUpload(param, "/configurationmanage/notice/configNotice", addNoticeCallback);
}

function addNoticeCallback(response) {
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
<div class="dialogContent noticeAddPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>공지 등록 정보</h2>
		<p>필수 항목을 입력하고 첨부파일을 등록해 주세요.</p>
	</div>
	<form id="formAddConfigNotice" name="formAddConfigNotice">
		<ul class="section popupCard popupFormGrid">
			<li class="half">
				<custom:popupSelectBox options="${noticeType }" name="noticeType" label="form.configType" id="noticeType"/>
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUserNm" value="${sessionUser.userNm }" id="insertUserNm" readOnly="readOnly" />
			</li>
			<li class="full">
				<custom:popupInputText name="title" label="form.title" value="${title}" id="noticeTitle"  />
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
					<input type="file" id="noticeFile" name="noticeFile" onchange="fileChange()" style="display: none;"/>
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
		<custom:popupButton function="saveAddNotice()" name="save" label="btnAdd" id="save"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
