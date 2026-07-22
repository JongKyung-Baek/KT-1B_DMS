<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/disposalApproval/disposalApprovalPopup.js"></script>
<!-- 폐기 승인 팝업(폐기승인/반려 버튼)-->
<script>
	$(function() {
	});
</script>
<div class="dialogContent">
<form id="formDisposalApprovalPopup">
	<input type="hidden" id="destroyRequestNo" name="destroyRequestNo" value="${data.destroyRequestNo }" />
	<ul class="section">
		<li class="half">
			<%-- 요청자 --%>
			<custom:popupInputText name="insertUserNm" id="insertUserNm" label="form.requestUserName" value="${data.insertUserNm }" disabled="disabled"/>
		</li>
		<li>
			<%-- 폐기사유 --%>
			<custom:popupInputTextArea name="requestDesc" label="form.destroyDesc" rows="3" value="${data.requestDesc }" id="requestDesc" disabled="Y"/>
		</li>
		<li>
			<%-- 반려사유 --%>
			<custom:popupInputTextArea name="rejectDesc" label="form.rejectDesc" rows="3" value="" id="rejectDesc"/>
		</li>
	</ul>
</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 폐기승인 --%>
		<custom:popupButton function="disposalApproval('A')" name="approval" label="btn.destroyApproval" id="approval"/>
		<%-- 폐기반려 --%>
		<custom:popupButton function="disposalApproval('R')" name="reject" label="btn.destroyReject" id="reject"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>