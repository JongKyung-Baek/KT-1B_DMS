<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="/resources/js/views/inside/unregisted/request/distributionRequestPopup.js"></script>
<!-- 등록 및 배포요청 팝업(자료배포 버튼) -->
<style>
	.section li textarea{height:80px;}
</style>
<div class="dialogContent">
	<form id="formRequestPopup">
		<input type="hidden" id="objectType" name="objectType" value="${objectType }"/>
		<input type="hidden" id="businessAreaCd" name="businessAreaCd" value="${businessAreaCd }"/>
		<ul class="section">
			<li class="half">
				<%-- 업체 --%>
				<custom:popupSearchSelectBox options="${noSelect }" name="companyCd" label="form.company" searchUrl="/inside/authorization/selectCompanyCombo" id="companyCd"/>
				<%-- 업체 사용자 --%>
				<custom:popupSearchSelectBox options="${noSelect }" name="companyUserCd" label="form.companyUser" searchUrl="" id="companyUserCd"/>
			</li>
			<!-- <li class="half">

				<%-- Email --%>
				<custom:popupInputText name="deployUserEmail" label="form.email" value="${deployUserEmail }" id="deployUserEmail"/>
			</li> -->
			<li class="half">
				<%-- 구매담당자 참조메일--%>
<%-- 				<custom:popupInputText name="purchaseTeamNm" label="form.purcharserCC" value="${sessionUser.userNm }" id="purchaseTeamNm" disabled="disabled"/> --%>
				<%-- 구매담당자 --%>
				<custom:popupSelectBox options="${purchaseTeam }" selectedValue="${sessionUser.userCd }" name="purchaseTeam" label="form.approvalUser" id="purchaseTeam"/>
				<%-- 결재자 --%>
				<custom:popupSelectInputBox options="${teamLeaderList }" id="teamLeader" name="teamLeader" label="form.authorizePerson" selectedValue="${teamLeaderUid }"/>
			</li>
			<li>
				<custom:popupMultiSelectBox count="8" options="${securityUserList}" selectedValue="${defaultSecurityUser}" name="securityUserList" label="form.securityUserList" id="securityUserList" />
			</li>
		</ul>
		<ul class="section">
			<li class="distributionCheck">
				<%-- 배포방식 --%>
				<label for=""><spring:message code="form.distributionType"/></label>
				<div>
					<custom:popupRadio labelSide="right" options="${distributionMethod }" name="distributionType" checkedValue="viewPrint"/>
					<%--
					<div class="fileDistributionType">
						<custom:popupRadio labelSide="right" options="${distributionFileType }" name="fileDistributionType" disabled="disabled" checkedValue="security"/>
					</div>
					--%>
				</div>
			</li>
			<li class="half">
				<%-- 용도 --%>
				<custom:popupSelectBox options="${deployList }" name="requestPurpose" label="form.purpose" id="requestPurpose"/>
				<%-- 유효기간 --%>
				<custom:popupInputText name="deployTerm" label="form.validateTerm" value="1" id="deployTerm"/>
			</li>
			<li>
				<%-- 배포요청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.distributionRequestDesc" rows="2" value="" id="requestDesc" disabled="N"/>
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
		<p class="textCaution "><spring:message code="title.unregistedMsg" /></p>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left">
<%--		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>--%>
		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y" checked onclick="toggleCheckboxValue('emailCheck')">
		<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
		<script>
		$("#emailCheck").prettyCheckable({labelPosition: 'left'});
		</script>
	</div>
	<div class="right">
		<%-- 배포요청 --%>
		<custom:popupButton function="save()" name="save" label="btn.distributionRequest" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
