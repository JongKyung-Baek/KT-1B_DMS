<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript"
	src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/commonrequest/commonRequestPopup.js"></script>
<style>
	.commonRequestPopup .popupCard > li.distributionCheck > label{
		margin-bottom:6px;
	}
</style>
<script>
$(document).ready(function(){
	setGridParam();
	settingGridWithData('${gridInfo}', popupGridParam, 'popupGridParam');
});
</script>
<!-- 도면 팝업 (배포승인요청 버튼) -->
<!-- <div id="popupDialog1" class="dialogContainer"> -->
<div class="dialogContent commonRequestPopup popup-base popup-actions-center popup-type-upload-grid">
	<div class="popupHero">
		<h2>배포 승인 요청</h2>
		<p>요청 정보를 입력한 뒤 배포 승인 요청을 진행해 주세요.</p>
	</div>
	<form id="formRequest">
		<input type="hidden" value="${requestType }" id="requestType" name="requestType" />
		<input type="hidden" id="developmentProtectUserUid" name="developmentProtectUserUid" />
		<input type="hidden" id="productProtectUid" name="productProtectUid" />
		<input type="hidden" id="businessAreaCd" name="businessAreaCd" value=""/>
		<ul class="section popupCard popupFormGrid popup-grid-4">
			<li>
				<%-- 업체 --%>
				<custom:popupSearchSelectBox options="${noSelect }" name="companyCd" label="form.company" searchUrl="/inside/authorization/selectCompanyCombo" id="companyCd" />
			</li>
			<li>
				<%-- 업체 사용자 --%>
<!--				<custom:popupSearchSelectBox options="${noSelect }" name="companyUserCd" label="form.companyUser" searchUrl="" id="companyUserCd" />-->
				<custom:popupSelectInputBox options="${noSelect }" name="companyUserCd" label="form.companyUser" id="companyUserCd" />
			</li>
			<li>
				<%-- 구매담당자--%>
				<custom:popupSelectBox options="${purchaseTeam }" selectedValue="${sessionUser.userCd }" name="purchaseTeam" label="form.approvalUser" id="purchaseTeam"/>
			</li>
			<c:if test="${isProductTeam == false}">
				<li>
					<%-- 결재??--%>
					<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="purchaseUid" label="form.authorizePerson" id="purchaseUid" />
				</li>
			</c:if>
			<%-- Email --%>
<%-- 				<custom:popupInputText name="deployUserEmail" label="form.email" --%>
<%-- 					value="${deployUserEmail }" id="deployUserEmail" /> --%>
<!-- 			<li class="half"> -->
<%-- 				<custom:popupSearchSelectBox options="${purchaseTeam }" --%>
<%-- 				name="purchaseTeam" label="form.approvalUser" --%>
<%-- 				searchUrl="/inside/authorization/selectPurchaseTeamCombo" id="purchaseTeam"/> --%>
<%-- 				<custom:popupInputText name="purchaseTeamNm" --%>
<%-- 					label="form.approvalUser" value="${sessionUser.userNm }" --%>
<%-- 					id="purchaseTeamNm" disabled="disabled" /> <input type="hidden" --%>
<%-- 				name="purchaseTeam" id="purchaseTeam" value="${sessionUser.userCd }" /> --%>
<!-- 			</li> -->
<!-- 			<li class="half"> -->
<!-- 			</li> -->
			<c:if test="${fn:contains(requestType, 'PRODUCT') == false}">
				<li>
				<%-- 방산기술책임??--%>
					<custom:popupMultiSelectBox rows="2" count="${protectUserCount }" name="protectUserUid" id="protectUserUid" label="form.protectiveOfficerUid" options="${protectUserList }" selectedValue="${defaultProtectUserList }" disabled="${protectYn }"/>
				</li>
			</c:if>
<!-- 			<li class="half"> -->
<%-- 				개발 방산기술보호 결재??<custom:popupSelectInputBox --%>
<%-- 					options="${developmentProtectUserList }" --%>
<%-- 					selectedValue="${developmentProtectUserList[0] }" --%>
<%-- 					disabled="${developmentProtectYn}" name="developmentProtectUserUid" --%>
<%-- 					label="form.developmentProtectUid" id="developmentProtectUserUid" /> --%>
<%-- 				생산 방산기술보호 결재자 <custom:popupSelectInputBox --%>
<%-- 					options="${productProtectUserList }" --%>
<%-- 					selectedValue="${productProtectUserList[0] }" --%>
<%-- 					disabled="${productProtectYn}" name="productProtectUid" --%>
<%-- 					label="form.productProtectUid" id="productProtectUid" /> --%>
<!-- 			</li> -->
			<li class="half">
				<%-- 용도 --%> <custom:popupSelectBox options="${deployList }"
					name="requestPurpose" label="form.purpose" id="requestPurpose" /> <%-- 유효기간 --%>
			</li>
			<c:if test="${fn:contains(requestType, 'SW') == false}">
			<li class="distributionCheck">
				<!--  배포방식 --> <label for=""><spring:message code="form.distributionType" /></label>
				<div>
					<custom:popupRadio labelSide="right" options="${distributionMethod }" name="distributionType" checkedValue="viewPrint" />

					<div class="fileDistributionType" style="display: none;"> <%-- 07.21 기범 추가: style 숨기지 않으면 일반/보안이 즉시 보입니다. disabled 제거 시 일반/보안 체크가 가능합니다. --%>
						<custom:popupRadio labelSide="right" options="${distributionFileType}" name="fileDistributionType" disabled="disabled" checkedValue="security"  />
					</div>

				</div>
			</li>
			</c:if>
			
			
			<c:if test="${fn:contains(requestType, 'SW') == true}">
			<li class="distributionCheck full">
				<!--  배포방식 --> <label for=""><spring:message code="form.distributionType" /></label>
				<div>
					<custom:popupRadio labelSide="right" options="${distributionMethod_sw }" name="distributionType" checkedValue="viewPrint" />
					<div class="fileDistributionType">
						<custom:popupRadio labelSide="right" options="${distributionFileType_sw }" name="fileDistributionType" disabled="disabled" checkedValue="general" />
					</div>
				</div>
			</li>
			</c:if>
			<li class="full">
				<label>요청사유</label>
				<div class="gridContainer">
					<table id="gridRequestUseEndYmdPopup"></table>
				</div>
			</li>
			<li class="full">
				<%-- 요청사유 --%> <custom:popupInputTextArea name="requestDesc"
					label="form.applyReason" rows="2" value="" id="requestDesc"
					disabled="N" />
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" value="Y"/>
	</form>
	<div class="dialogBtnSet">
		<div class="left" style="display: block;">
			<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>
			<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
			<script>
			$("#emailCheck").prettyCheckable({labelPosition: 'left'});
			</script>
		</div>
		<div class="right" style="text-align: center;">
			<%-- 배포승인요청 --%>
			<custom:popupButton function="distributionApprovalRequest()"
				name="reqDeploymentApproval" label="btn.reqDeploymentApproval"
				id="reqDeploymentApproval" />
			<custom:popupButton function="closePopup('popupDialog')" name="close"
				label="btn.close" id="close" />
		</div>
	</div>
</div>
<!-- </div> -->
