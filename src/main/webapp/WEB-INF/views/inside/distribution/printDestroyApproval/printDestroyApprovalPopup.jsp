<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/printDestroyApproval/printDestroyApprovalPopup.js"></script>
<!-- 출력 폐기 승인 팝업(요청번호 상세보기) -->
<script>
var popupGridParam;
var popupGridId='gridApproval';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam());
});

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'DISTRIBUTION\', \'"+rowdata["objectType"]+"\', \'" +rowdata["requestNo"] +"\', \'" + rowdata["objectId"] +"\', \'" + rowdata["fileNo"] + "\', \'" + rowdata["protectYn"] + "\')">'+cellValue+'</a>';
}

</script>
<div class="dialogContent commonRequestPopup printApprovalPopup printDestroyApprovalPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>출력 폐기 승인</h2>
		<p>요청 정보를 확인하고 승인 또는 반려를 진행해 주세요.</p>
	</div>
	<form id="formApprovalPopup">
		<input type="hidden" name="destroyRequestNo" id="destroyRequestNo" value="${destroyRequestNo}">
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<custom:popupInputTextArea name="requestDesc" label="form.printDestoryRequestDesc" rows="2" value="${data.requestDesc }" id="requestDesc" disabled="Y"/>
			</li>
			<li class="half">
				<custom:popupInputTextArea name="rejectDesc" label="form.printDestoryRejectDesc" rows="2" value="" id="rejectDesc"/>
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code="title.printDestroyApprovalList"/></span><span class="listCount" id="listCount"></span>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridApproval"></table>
		</div>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<custom:popupButton function="saveDestroy('A')" name="approval" label="btn.destroyApproval" id="approval"/>
			<custom:popupButton function="saveDestroy('R')" name="reject" label="btn.destroyReject" id="reject"/>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
</div>
