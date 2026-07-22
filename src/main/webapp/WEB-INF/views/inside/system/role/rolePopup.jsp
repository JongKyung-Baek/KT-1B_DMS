<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/role/rolePopup.js"></script>
<div class="dialogContent commonRequestPopup rolePopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
<div class="popupHero">
	<h2><spring:message code="label.roleGroup" text="권한 그룹" /></h2>
	<p>권한 그룹명을 입력한 뒤 저장할 수 있습니다.</p>
</div>
<form id="formPopup">
	<input type="hidden" name="groupCd" id="groupCd" value="${vo.groupCd }"/>
	<input type="hidden" name="saveFlag" id="saveFlag" value="${saveFlag }"/>
	<ul class="section popupCard popupFormGrid popup-grid-1">
		<li class="full">
			<%-- 권한그룹명 --%>
			<custom:popupInputText name="groupNm" id="groupNm" label="label.roleGroupNm" value="${vo.groupNm }" />
		</li>
	</ul>
</form>
</div>
<div class="dialogBtnSet">
	<div class="left">
	</div>
	<div class="right">
		<%-- 승인요청  --%>
		<custom:popupButton function="savePopup()" name="save" label="btn.save" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
