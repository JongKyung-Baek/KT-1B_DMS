<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/configurationmanage/notice/noticePopup.js"></script>
<script>
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
	$(".ui-dialog .ui-dialog-titlebar-close").click(function(event){
		console.log("close-popup");
		closePopup('popupDialog');
	});
});

</script>
<style>
	textarea#contents{height:180px;}
	.ui-dialog ul.section li div.fileDownload{border:0;}
	.fileDownBtn{margin:0; margin-right:3px;}
	.noticeDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload,
	.noticeDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload .singleFileUpload { display:none; }
</style>
<c:if test="${empty formData.fileList && sessionUser.roleGroup ne 'RG_001'}">
	<style>
		textarea#contents{height:220px;}
	</style>
</c:if>
<c:choose>
	<c:when test="${updateFlag == 'Y' }">
		<c:set var="disabled" value="disabled"/>
	</c:when>
</c:choose>
<div class="dialogContent noticeDetailPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>공지 상세 정보</h2>
		<p>공지 내용을 확인하고 필요 시 수정해 주세요.</p>
	</div>
	<form id="formConfigNoticePopup">
		<input type="hidden" name="fileDelete" id="fileDelete" value="false">
		<input type="hidden" name="noticeCd" id="noticeCd" value="${noticeCd}">
		<input type="hidden" name="noticeHit" id="noticeHit" value="${data.noticeHit}">
		<ul class="section popupCard popupFormGrid">
			<li class="half">
				<custom:popupSelectBox options="${noticeType}" selectedValue="${data.noticeType}" name="noticeType" label="form.configType" id="noticeType" disabled="${updateFlag}"/>
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUserNm" value="${data.insertUserNm }" id="insertUserNm" disabled="disabled"/>
			</li>
			<li class="full">
				<custom:popupInputText name="title" label="form.title" value="${data.title }" id="noticeTitle" disabled="${disabled}"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${data.contents }" id="contents" readOnly="${updateFlag}"/>
			</li>
			<c:if test="${sessionUser.roleGroup ne 'RG_001'}">
			<li class="singleFileUpload full">
				<label><spring:message code="grid.appendFile"/></label>
				<div></div>
			</li>
			</c:if>
		</ul>
		<c:if test="${not empty formData.fileList }">
		<ul class="section popupCard popupFormGrid appendFile uploadOnly">
		<li class="appendFile full">
			<label for=""><spring:message code="grid.appendFile" /></label>
			<div class="fileDownload">
				<c:forEach items="${formData.fileList }" var="fileInfo">
					<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( ${fileInfo.noticeCd}, ${fileInfo.fileNo }, "${fileInfo.fileNm }")' ><span>${fileInfo.fileNm }</span></button>
				</c:forEach>
				<c:if test="${sessionUser.roleGroup eq 'RG_001'}">
				<button type="button" class="ui-button ui-corner-all removeBtn" onclick='removeFile()' ><span></span></button>
				</c:if>
			</div>
		</li>
		</ul>
		</c:if>
		<c:if test="${empty formData.fileList}">
		<script>
			$(".singleFileUpload").show();
		</script>
		</c:if>
	</form>
	<c:if test="${sessionUser.roleGroup eq 'RG_001'}">
	<ul class="section popupCard popupFormGrid singleFileUpload uploadOnly">
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
	</c:if>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 수정 --%>
		<c:if test="${sessionUser.roleGroup eq 'RG_001'}">
		<custom:popupButton function="updateNotice()" name="save" label="btnUpdate" id="save"/>
		</c:if>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
	<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">
	</form>
