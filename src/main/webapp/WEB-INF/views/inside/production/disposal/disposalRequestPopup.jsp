<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/disposal/disposalRequestPopup.js"></script>
<!-- 현황 및 폐기 팝업(전체폐기 버튼)-->
<script>
</script>
<style>
	.section li textarea{height:100px;}
</style>
<div class="dialogContent">
<form id="formDisposalRequestPopup">
	<ul class="section">
		<li>
			<%-- 요청자 --%>
			<custom:popupInputText name="requestUser" id="requestUser" label="form.requestUserName" value="${sessionUser.userNm }" disabled="disabled"/>
			<input type="hidden" name="requestUserCd" id="requestUserCd" value="${sessionUser.userCd }"/>
		</li>
		<li>
			<%-- 결재자 --%>	<%-- 팀장 + 팀원 --%>
			<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="teamLeaderUid" label="form.authorizePerson" id="teamLeaderUid"/>
		</li>
		<li>
			<custom:popupInputTextArea name="requestDesc" label="form.destroyDesc" rows="3" value="" id="requestDesc"/>
		</li>
	</ul>
	<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
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
		<%-- 승인요청  --%>
		<custom:popupButton function="approvalRequest()" name="request" label="btn.approvalRequest" id="request"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
