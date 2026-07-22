<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/printHistory/disposalRequestPopup.js"></script>
<!-- 출력물 이력 - 출력물 폐기요청 팝업 -->
<script>
</script>
<div class="dialogContent commonRequestPopup printHistoryDisposalRequestPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>출력물 폐기 요청</h2>
		<p>요청 정보를 입력하고 출력물 폐기 승인 요청을 진행해 주세요.</p>
	</div>
	<form id="destoryRequest">
		<input type="hidden" id="developmentProtectUserUid" name="developmentProtectUserUid" />
		<input type="hidden" id="productProtectUid" name="productProtectUid" />

		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<custom:popupInputText name="requestUser" label="form.requestUserName" value="${sessionUser.userNm }" id="requestUser" disabled="disabled"/>
			</li>
			<li class="half">
				<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="teamLeaderUid" label="form.authorizePerson" id="teamLeaderUid"/>
			</li>
		</ul>

		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half full-row">
				<custom:popupInputTextArea name="requestDesc" label="form.applyReason" rows="2" value="" id="requestDesc"/>
			</li>
		</ul>
	</form>

	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<custom:popupButton function="destroyRequest()" name="req" label="btn.destroyApprovalRequest" id="req"/>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
</div>
