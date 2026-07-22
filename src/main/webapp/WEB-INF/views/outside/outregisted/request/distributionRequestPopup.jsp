<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="/resources/js/views/outside/outregisted/request/distributionRequestPopup.js"></script>
<!-- 자료전송 팝업 버튼) -->
<style>
	.section li textarea{height:80px;}
</style>
<div class="dialogContent">
	<form id="formRequestPopup">
		<input type="text" id="deployUserEmail" name="deployUserEmail" value="${sessionUser.email }">
		<input type="text" id="receiveUserEmail" name="receiveUserEmail" value="">
		<ul class="section">
			<li class="half">
				<%-- 업체 사용자 --%>
				<custom:popupSelectBox options="${deployUser }" selectedValue="${sessionUser.userCd}" defaultText="form.select" name="deployUserCd" label="form.companyUser" id="deployUserCd"/>
			</li>
			<li>
				<%-- 수신자 --%>
				<custom:popupSelectInputBox options="${receiveUser }" name="receiveUserCd" label="form.receiveUser" id="receiveUserCd"/>
			</li>
			<li class="half">
				<%-- 용도 --%>
				<custom:popupSelectBox options="${requestPurpose }" defaultText="form.select" name="requestPurpose" label="form.purpose" id="requestPurpose"/>
				<%-- 유효기간 --%>
				<custom:popupInputText name="deployTerm" label="form.validateTerm" value="1" id="deployTerm"/>
			</li>
		</ul>
		
		<ul class="section">
			<li>
				<%-- 자료전송사유 --%>
				<custom:popupInputTextArea name="transDesc" label="form.transDesc" rows="2" value="" id="transDesc" disabled="N"/>
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
<%--		<p class="textCaution "><spring:message code="title.unregistedMsg" /></p> --%>
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
		<custom:popupButton function="save()" name="save" label="btn.outregistedTrans" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>