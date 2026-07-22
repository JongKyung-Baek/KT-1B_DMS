<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/approvalPopup.js"></script>
<!-- 배포 승인-->
<script>
var popupGridParam;
var gridId = 'gridDistributionApprovalPopup';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam());
});


function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridDistributionApprovalPopup',
			formId : 'formAcceptancePopup',
			url : '/inside/distribution/acceptance/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}
</script>
<style>
	.section li textarea{height:80px;}
</style>
	<div class="dialogContent">
		<form id="formAcceptancePopup">
			<input id="requestNo" name="requestNo" value="${data.requestNo}" type="hidden"/>
			<input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
			<input id="approvalLineId" name="approvalLineId" value="${data.approvalLineId}" type="hidden"/>

			<ul class="section">
				<li class="half">
					<%-- 변경요청사유 --%>
					<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" rows="2" value="${data.requestDesc} " id="requestDesc" disabled="Y"/>
					<%-- 배포요청 반려 사유 --%>
					<custom:popupInputTextArea name="rejectRequestDesc" label="form.rejectRequestDesc" rows="2" value="" id="rejectRequestDesc"/>
				</li>
			</ul>
			<ul class="section">
				<li class="third">
					<%-- 용도 --%>
					<custom:popupSelectBox options="${requestPurpose }" selectedValue="${data.requestPurpose }" name="requestPurpose" label="form.purpose" id="requestPurpose" disabled="Y"/>
					<%-- 유효기간(개월) --%>
					<custom:popupInputText name="deployTerm" id="deployTerm" label="form.deployTerm" value="${data.deployTerm }"/>
					<%-- 배포방식 --%>
					<label for=""><spring:message code="form.distributionType"/></label>
					<div>
						<input type="text" value='<spring:message code="combo.viewPrint"/>' disabled>
<!-- 					<label for="">Viewing/출력</label> -->
<%-- 					<custom:popupRadio labelSide="right" options="${distributionMethod }" name="distributionType" checkedValue="viewPrint"/> --%>
<!-- 					<div class="fileDistributionType"> -->
<%-- 						<custom:popupRadio labelSide="right" options="${distributionFileType }" name="fileDistributionType" disabled="disabled" checkedValue="general"/> --%>
<!-- 					</div> -->
					</div>
				</li>
				<li class="third">
					<%-- 결재자 --%>
					<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="purchaseUid" label="form.authorizePerson" id="purchaseUid"/>
				</li>
<!-- 			<li class="distributionCheck"> -->
			</ul>
			<div class="section">
				<div class="dialogToolbar">
					<div class="left">
						<span class="gridTitle">배포요청 접수목록</span><span class="listCount" id="listCount"></span>
					</div>
					<div class="right">
						<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button>
					</div>
				</div>
				<div class="gridContainer">
					<table id="gridDistributionApprovalPopup"></table>
				</div>
			</div>
		</form>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<%-- 요청이 아닐 경우 승인 반려 버튼 숨기기 --%>
			<c:set var="requestCd" value="${requestCd }" />
			<c:if test="${requestCd eq 'REQUEST'}">
			<%-- 승인요청 --%>
			<custom:popupButton function="saveAcceptance('A')" name="approvalRequest" label="btn.approvalRequest" id="approvalRequest"/>
			<%-- 승인반려 --%>
			<custom:popupButton function="saveAcceptance('R')" name="approvalReject" label="btn.approvalReject" id="approvalReject"/>
			</c:if>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
<!-- </div> -->
