<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/printApproval/printApprovalPopup.js"></script>
<!-- 출력 승인 - 도면/문서/SW 팝업(출력요청번호 상세보기) -->
<script>
var popupGridParam;
var popupGridId = 'gridPrintApprovalPopupList';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});

function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridPrintApprovalPopupList',
			formId : 'formPrintApprovalPopup',
			url : '/inside/distribution/approval/selecApprovalList',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}

	return popupGridParam;
}

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'DISTRIBUTION\', \'"+rowdata["objectType"]+"\', \'" +rowdata["requestNo"] +"\', \'" + rowdata["objectId"] +"\', \'" + rowdata["fileNo"] + "\', \'" + rowdata["protectYn"] + "\')">'+cellValue+'</a>';
}
</script>
<div class="dialogContent commonRequestPopup printApprovalPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>출력 승인</h2>
		<p>요청 정보를 확인하고 승인 또는 반려를 진행해 주세요.</p>
	</div>
	<form id="formPrintApprovalPopup">
		<input type="hidden" value="${requestNo }" id="requestNo" name="requestNo"/>
		<input type="hidden" value="${objectType }" id="objectType" name="objectType"/>
		<input type="hidden" value="PRINT" id="requestType" name="requestType"/>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<%-- 출력요청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.printApprovalRequestDesc" rows="2" value="${requestDesc }" id="requestDesc" disabled="Y"/>
			</li>
			<li class="half">
				<%-- 출력반려사유 --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.printRejectDesc" rows="2" value="" id="rejectDesc"/>
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 출력승인 목록 --%>
				<span class="gridTitle"><spring:message code="title.printApprovalList"/></span><span class="listCount">${listCount }</span>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridPrintApprovalPopupList"></table>
		</div>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<%-- 출력승인 --%>
			<custom:popupButton function="saveApproval('A')" name="printApproval" label="btn.printApproval" id="printApproval"/>
			<%-- 출력반려 --%>
			<custom:popupButton function="saveApproval('R')" name="approvalReject" label="btn.printReject" id="approvalReject"/>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
</div>
