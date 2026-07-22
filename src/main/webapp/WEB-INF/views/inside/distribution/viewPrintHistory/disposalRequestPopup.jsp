<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/printHistory/disposalRequestPopup.js"></script>
<!-- 출력물 이력 - 도면/문서/SW 팝업(출력물폐기요청 버튼)-->
<script>
</script>
<style>
	.section li textarea{height:80px;}
</style>
<div class="dialogContent">
	<form id="destoryRequest">
		<input type="hidden" id="developmentProtectUserUid" name="developmentProtectUserUid" />
		<input type="hidden" id="productProtectUid" name="productProtectUid" />
		<ul class="section">
			<li class="half">
				<%-- 요청자 --%>
				<custom:popupInputText name="requestUser" label="form.requestUserName" value="${sessionUser.userNm }" id="requestUser" disabled="disabled"/>
				<%-- 결재자 --%>
				<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="teamLeaderUid" label="form.authorizePerson" id="teamLeaderUid"/>
			</li>
			<%-- <li class="half">				방산기술보호 책임자 --%>
<%-- 				<custom:popupSelectInputBox options="${defendTeamLeaderList }" selectedValue="${defendTeamLeaderUid }" disabled="${protectYn}" name="protectiveOfficerUid" label="form.protectiveOfficerUid" id="protectiveOfficerUid"/> --%>
<!-- 			</li>
			<li> -->
				<%-- 방산기술책임자 --%>
<%-- 				<custom:popupMultiSelectBox rows="2" count="${protectUserCount }" name="protectUserUid" id="protectUserUid" label="form.protectiveOfficerUid" options="${protectUserList }" selectedValue="${defaultProtectUserList }" disabled="${protectYn }"/> --%>
<!-- 			</li> -->
<!-- 			<li class="half"> -->
<%-- 				개발 방산기술보호 결재자 --%>
<%-- 				<custom:popupSelectInputBox options="${developmentProtectUserList }" selectedValue="${developmentProtectUserList[0] }" disabled="${developmentProtectYn}" name="developmentProtectUserUid" label="form.developmentProtectUid" id="developmentProtectUserUid" /> --%>
<%-- 				양산 방산기술보호 결재자 --%>
<%-- 				<custom:popupSelectInputBox options="${productProtectUserList }" selectedValue="${productProtectUserList[0] }" disabled="${productProtectYn}" name="productProtectUid" label="form.productProtectUid" id="productProtectUid" /> --%>
<!-- 			</li> -->
		</ul>
		<ul class="section">
			<li>
				<%-- 신청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.applyReason" rows="2" value="" id="requestDesc"/>
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 출력승인요청 --%>
		<custom:popupButton function="destroyRequest()" name="req" label="btn.destroyApprovalRequest" id="req"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>