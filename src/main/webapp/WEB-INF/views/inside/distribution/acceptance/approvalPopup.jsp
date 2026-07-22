<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/approvalPopup.js"></script>
<!-- 배포 승인 -->
<script>
var popupGridParam;
var popupGridId='gridDistributionApprovalPopup';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam());
});

function formatOpenView(cellValue, options, rowdata, action){
// 	return '<a onclick="openFile(\''+ rowdata["filePath"] +'\')">'+cellValue+'</a>';
	return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
}

function setPopupGridParam(){
	popupGridParam = {
			gridId : popupGridId,
			formId : 'formAcceptancePopup',
			url : '/inside/distribution/acceptance/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}

	return popupGridParam;
}

function formatAddMonth(cellValue, options, rowdata, action){
	if(undefined == cellValue){
		return '';
	}else{
		return cellValue + g_msg("msg.month");
	}
}
</script>
<style>
	.acceptanceApprovalPopup .section li textarea{height:80px;}
</style>
	<div class="dialogContent commonRequestPopup acceptanceApprovalPopup popup-base popup-actions-center popup-type-form-grid">
		<div class="popupHero">
			<h2>배포 승인</h2>
			<p>요청 정보를 확인하고 승인 또는 반려를 진행해 주세요.</p>
		</div>
		<form id="formAcceptancePopup">
			<input id="requestNo" name="requestNo" value="${data.requestNo}" type="hidden"/>
			<input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
			<input id="approvalLineId" name="approvalLineId" value="${data.approvalLineId}" type="hidden"/>
			<input id="requestPurposeData" name="requestPurposeData" value="${data.requestPurpose }" type="hidden" />
			<ul class="section popupCard popupFormGrid popup-grid-2">
				<li class="half">
					<%-- 요청사유 --%>
					<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" rows="2" value="${data.requestDesc}"  id="requestDesc" disabled="Y"/>
				</li>
				<li class="half">
					<c:if test="${requestCd eq 'REQUEST'}">
						<custom:popupInputTextArea name="rejectRequestDesc" label="form.rejectRequestDesc" rows="2" value="" id="rejectRequestDesc" />
					</c:if>
					<c:if test="${requestCd ne 'REQUEST'}">
						<custom:popupInputTextArea name="rejectRequestDesc" label="form.rejectRequestDesc" rows="2" value="${data.rejectDesc}" id="rejectRequestDesc" disabled="Y"/>
					</c:if>
					<%-- 반려사유 --%>
				</li>
			</ul>
						<ul class="section popupCard popupFormGrid popup-grid-2">
				<li class="half">
					<custom:popupSelectBox options="${requestPurpose }" selectedValue="${data.requestPurpose }" name="requestPurpose" label="form.purpose" id="requestPurpose" disabled="Y"/>
				</li>
				<li class="half">
					<c:if test="${requestCd eq 'REQUEST'}">
						<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="purchaseUid" label="form.authorizePerson" id="purchaseUid"/>
					</c:if>
					<c:if test="${requestCd ne 'REQUEST'}">
						<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${data.approvalUserCd }" name="purchaseUid" label="form.authorizePerson" id="purchaseUid" disabled="Y"/>
					</c:if>
				</li>
				<li class="half">
					<label for=""><spring:message code="form.distributionType"/></label>
					<div>
						<input type="text" value="${data.distributeMethodNm}" disabled>
					</div>
				</li>
				<c:if test="${fn:contains(objectType, 'PRODUCT') == false}">
					<li class="half">
						<custom:popupSelectInputBox options="${defendTeamLeaderUid }" selectedValue="${defendTeamLeaderUid[0] }" disabled="${defDisabledYn}" name="protectiveOfficerUid" label="form.protectiveOfficerUid" id="protectiveOfficerUid"/>
					</li>
				</c:if>
			</ul>
			<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="N"/>
		</form>
		<div class="section popupCard">
			<div class="dialogToolbar">
				<div class="left">
						<span class="gridTitle">&#48176;&#54252;&#50836;&#52397; &#51217;&#49688;&#47785;&#47197;</span><span class="listCount" id="listCount"></span>
				</div>
<!-- 			<div class="right"> -->
<!-- 				<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
<!-- 			</div> -->
			</div>
			<div class="gridContainer">
				<table id="gridDistributionApprovalPopup"></table>
			</div>
		</div>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<%-- REQUEST 상태에서만 승인/반려 버튼 노출 --%>
			<c:set var="requestCd" value="${requestCd }"></c:set>
			<c:if test="${requestCd eq 'REQUEST'}">
				<%-- 승인요청 --%>
				<custom:popupButton function="saveAcceptance('A')" name="approvalRequest" label="btn.approvalRequest" id="approvalRequest"/>
				<%-- 승인반려 --%>
				<custom:popupButton function="saveAcceptance('R')" name="approvalReject" label="btn.approvalReject" id="approvalReject"/>
			</c:if>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>

