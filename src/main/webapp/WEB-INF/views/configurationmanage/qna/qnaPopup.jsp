<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/configurationmanage/qna/qnaPopup.js"></script>
<!-- QnA 팝업(QnA 상세보기)-->
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
	textarea{height:140px;}
	.ui-dialog ul.section li div.fileDownload{border:0; overflow-y:hidden;}
	.fileDownBtn{margin:0; margin-right:3px;}
	.fileDownBtn:only-child{margin-right:0;}
	.qnaDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload,
	.qnaDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload .singleFileUpload { display:none; }
</style>
<c:if test="${'Q' eq data.status}">
	<style>
		textarea#contents{height:auto; min-height:65px;}
		textarea#answer{height:110px; min-height:110px;}
	</style>
</c:if>
<c:if test="${'A' eq data.status}">
	<style>
		textarea#contents{height:auto; min-height:65px;}
		textarea#answer{height:auto; min-height:80px;}
	</style>
</c:if>
<c:if test="${'A' eq data.status && empty formData.fileList}">
	<style>
		textarea#contents{height:auto; min-height:75px;}
		textarea#answer{height:auto; min-height:90px;}
	</style>
</c:if>

<c:choose>
	<c:when test="${ fn:length(data.contents) > 200}">
		<c:set var="contentsrows" value="10"/>
	</c:when>
	<c:when test="${ fn:length(data.contents) > 100}">
		<c:set var="contentsrows" value="5"/>
	</c:when>
	<c:otherwise>
		<c:set var="contentsrows" value="1"/>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${ fn:length(data.answer) > 200}">
		<c:set var="answerrows" value="10"/>
	</c:when>
	<c:when test="${ fn:length(data.answer) > 100}">
		<c:set var="answerrows" value="5"/>
	</c:when>
	<c:otherwise>
		<c:set var="answerrows" value="1"/>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${'Q' eq data.status}">
		<c:set var="answerWaiting" value="disabled"/>
		<c:set var="answerWaitingYn" value="Y"/>
		<c:set var="answerComplete" value=""/>
		<c:set var="answerCompleteYn" value=""/>
	</c:when>
	<c:when test="${'A' eq data.status}">
		<c:set var="answerWaiting" value="disabled"/>
		<c:set var="answerWaitingYn" value="Y"/>
		<c:set var="answerComplete" value="disabled"/>
		<c:set var="answerCompleteYn" value="Y"/>
	</c:when>
</c:choose>
<div class="dialogContent qnaDetailPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>Q&A 상세 정보</h2>
		<p>문의 내용을 확인하고 답변을 등록해 주세요.</p>
	</div>
	<form id="formConfigQnaPopup">
		<input type="hidden" name="qnaCd" id="qnaCd" value="${data.qnaCd}">
		<input type="hidden" name="fileDelete" id="fileDelete" value="false">
		<ul class="section popupCard popupFormGrid">
			<li class="half">
				<custom:popupSelectBox options="${qnaType}" selectedValue="${data.qnaType}" name="qnaType" label="form.configType" id="qnaType" disabled="${answerWaitingYn}"/>
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUserNm"  value="${insertUserNm}" id="insertUserNm" disabled="disabled" />
			</li>
			<li class="full">
				<custom:popupInputText name="companyNm" label="form.companyNm"  value="${companyNm}" id="companyNm" disabled="disabled" />
			</li>
			<li class="full">
				<custom:popupInputText name="title" label="form.title" value="${data.title}" id="qnaTitle" disabled="${answerWaiting}"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="${contentsrows}" value="${data.contents}" id="contents" readOnly="${answerWaitingYn}"/>
			</li>
			<c:if test="${not empty formData.fileList }">
			<li class="appendFile full">
				<label for=""><spring:message code="grid.appendFile" /></label>
				<div class="fileDownload">
					<c:forEach items="${formData.fileList}" var="fileInfo">
					<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( ${fileInfo.qnaCd}, ${fileInfo.fileNo}, "${fileInfo.fileNm}")' ><span>${fileInfo.fileNm}</span></button>
					</c:forEach>
				</div>
			</li>
			</c:if>
		</ul>
		<%-- 답변 --%>
		<c:if test="${('Q' eq data.status) || ('A' eq data.status)}">
		<ul class="section popupCard popupFormGrid" style="margin-top:2px; margin-bottom:6px;">
			<li class="half">
				<custom:popupInputText name="answerUserNm" label="form.answerUserNm"  value="${data.answerUserNm}" id="answerUserNm" disabled="${answerComplete}"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="answer" label="form.answer" rows="${answerrows}" value="${data.answer}" id="answer" readOnly="${answerCompleteYn}"/>
			</li>
		</ul>
		</c:if>
	</form>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<c:if test="${'Q' eq data.status}">
			<button class="ui-button ui-corner-all bottomBtn" onclick="saveReplyQna()"><spring:message code="btnReply" /></button>
		</c:if>
		<custom:popupButton function="closePopup('qnaPopup')" name="close" label="btn.close" id="close"/>
	</div>
</div>
