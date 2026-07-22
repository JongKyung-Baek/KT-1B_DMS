<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/outside/outregisted/requeststatus/requestStatusPopup.js"></script>
<!-- 배포요청 현황 팝업(요청번호 상세보기) -->
<script>
var popupGridParam;
var popupGridId="gridRequestStatusPopup";

$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'UNREG_DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'1\')">'+cellValue+'</a>';
}
</script>
<style>
	div.section{margin-bottom:10px;}
</style>
<div class="dialogContent requestStatusPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2><spring:message code="title.requestStatus" text="요청 현황" /></h2>
	</div>
<%-- 	<input type="hidden" id="printYn" name="printYn" value="${data.printYn }"> --%>
<%-- 	<input type="hidden" id="deployNormalYn" name="deployNormalYn" value="${data.deployNormalYn }"> --%>
<%-- 	<input type="hidden" id="deploySpecialYn" name="deploySpecialYn" value="${data.deploySpecialYn }"> --%>
	<ul class="section popupCard">
		<li class="half">
			<%-- 업체 사용자 --%>
			<custom:popupInputText name="deployUserNm" id="deployUserNm" label="form.companyUser" value="${data.deployUserNm }" disabled="disabled"/>
			<%-- 수신자 --%>
			<custom:popupInputText name="receiveUserNm" id="receiveUserNm" label="form.receiveUser" value="${data.receiveUserNm }" disabled="disabled"/>
		</li>
		<li class="half">
			<%-- 용도 --%>
			<custom:popupInputText name="requestPurpose" id="requestPurpose" label="form.purpose" value="${data.requestPurpose }" disabled="disabled"/>
			<%-- 유효기간 --%>
			<custom:popupInputText name="deployTerm" id="deployTerm" label="form.validateTerm" value="${data.deployTerm }" disabled="disabled"/>
		</li>
		<li>
			<%-- 자료전송사유 --%>
			<custom:popupInputTextArea name="transDesc" label="form.requestReason" rows="2" value="${data.transDesc }" id="transDesc" disabled="Y"/>
		</li>
		
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 자료전송목록 --%>
				<span class="gridTitle"><spring:message code="title.outregTransList"/></span><span class="listCount" id="listCount"></span>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridRequestStatusPopup"></table>
		</div>
	</div>
	<form id="formRequestStatusPopup">
		<input type="hidden" id="requestNo" name="requestNo" value="${requestNo }"/>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
