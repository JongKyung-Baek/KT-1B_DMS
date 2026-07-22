<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/commonrequest/commonPrintRequestPopup.js"></script>
<!-- 출력 팝업 (출력승인요청 버튼) -->
<style>
	.commonPrintRequestPopup .textCaution{
		margin:0 0 8px 0;
		color:#7a7f93;
		font-size:13px;
		line-height:1.4;
		text-align:center;
	}

	.commonPrintRequestPopup input[type="date"]{
		width:100% !important;
		box-sizing:border-box;
	}

	/* keep calendar icon inside input (restore previous behavior) */
	.commonPrintRequestPopup .input-append.date input.hasDatepicker{
		border-right-width:1px !important;
		border-radius:4px !important;
	}

	.commonPrintRequestPopup .input-append.date > .ui-datepicker-trigger{
		background-color:#fff !important;
		display:block !important;
		z-index:3 !important;
	}

	/* dialogBtnSet is rendered outside .dialogContent in this jsp */
	.commonPrintRequestPopup + .dialogBtnSet{
		margin-top:6px;
		padding-top:0;
	}

	/* calendar popup text rendering fix (datepicker/select2 font fallback) */
	.ui-datepicker,
	.ui-datepicker *,
	.ui-datepicker .ui-datepicker-title,
	.ui-datepicker table th,
	.ui-datepicker table td,
	.ui-datepicker .ui-datepicker-buttonpane button,
	.ui-datepicker .select2-container,
	.ui-datepicker .select2-selection__rendered,
	.ui-datepicker .select2-results__option{
		font-family:dotum, tahoma, sans-serif !important;
		font-size:13px !important;
	}
</style>
<div class="dialogContent commonPrintRequestPopup popup-base popup-actions-center popup-type-upload-grid">
<form id="printRequestForm">
	<input type="hidden" value="${requestType }" id="requestType" name="requestType"/>
	<input type="hidden" id="developmentProtectUserUid" name="developmentProtectUserUid" />
	<input type="hidden" id="productProtectUid" name="productProtectUid" />
		<div class="popupHero">
			<h2>출력 승인 요청</h2>
			<p>요청 정보를 입력한 뒤 출력 승인 요청을 진행해 주세요.</p>
		</div>
		<p class="textCaution">출력기간 : 팀장 승인 후 1주일 / 출력 횟수 : 3회</p>
		<ul class="section popupCard popupFormGrid popup-grid-4">
			<li>
				<%-- 요청자 --%>
				<custom:popupInputText name="requestUser" label="form.requestUserName" value="${requestUser }" id="requestUser" disabled="disabled"/>
			</li>
			<li>
				<%-- 결재자 --%>
				<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="purchaseUid" label="form.authorizePerson" id="purchaseUid"/>
				<%-- 방산기술보호 책임자 --%>
<%-- 				<custom:popupSelectInputBox options="${defendTeamLeaderList }" selectedValue="${defendTeamLeaderUid }" disabled="${protectYn}" name="protectiveOfficerUid" label="form.protectiveOfficerUid" id="protectiveOfficerUid" /> --%>
			</li>
			<li>
				<%-- 방산기술보호 책임자 --%>
				<custom:popupMultiSelectBox rows="2" count="${protectUserCount }" name="protectUserUid" id="protectUserUid" label="form.protectiveOfficerUid" options="${protectUserList }" selectedValue="${defaultProtectUserList }" disabled="${protectYn }"/>
			</li>
<!-- 			<li class="half"> -->
<%-- 				개발 방산기술보호 책임자 --%>
<%-- 				<custom:popupSelectInputBox options="${developmentProtectUserList }" selectedValue="${developmentProtectUserList[0] }" disabled="${developmentProtectYn}" name="developmentProtectUserUid" label="form.developmentProtectUid" id="developmentProtectUserUid" /> --%>
<%-- 				생산 방산기술보호 책임자 --%>
<%-- 				<custom:popupSelectInputBox options="${productProtectUserList }" selectedValue="${productProtectUserList[0] }" disabled="${productProtectYn}" name="productProtectUid" label="form.productProtectUid" id="productProtectUid" /> --%>
<!-- 			</li> -->
			<li>
				<%-- 용도 --%>
				<custom:popupSelectBox options="${destroyType }" name="requestPurpose" label="form.purpose" id="requestPurpose"/>
<%-- 				<custom:popupSelectBox options="${deployList }" name="requestPurpose" label="form.purpose" id="requestPurpose"/> --%>
			</li>
			<li>
				<%-- 폐기기간 --%>
	<%-- 			<custom:popupInputText name="disposalTerm" label="form.disposalTerm" value="1개월" id="disposalTerm"/> --%>
				<label for="destroyTodoYmd"><spring:message code="form.disposalTerm"/></label>
				<input type="date" id="destroyTodoYmd" name="destroyTodoYmd" />
			</li>
			<li class="full">
				<%-- 신청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.applyReason" rows="2" value="" id="requestDesc"/>
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" value="Y"/>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left">
		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>
		<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
		<script>
		$("#emailCheck").prettyCheckable({labelPosition: 'left'});
		</script>
	</div>
	<div class="right">
		<%--승인요청 --%>
		<custom:popupButton function="approvalRequest()" name="reqPrintApproval" label="btn.approvalRequest" id="reqPrintApproval"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>

	</div>
</div>


