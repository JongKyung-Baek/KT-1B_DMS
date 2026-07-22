<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/outsideuser/userPopup.js"></script>
<style>
	.section .clearfixCheck label[for="protectYn"], .section .clearfixCheck label[for="crYn"], .section .clearfixCheck label[for="distributionYn"]{font-weight:600;}
</style>
<c:choose>
	<c:when test="${saveFlag == 'U' }">
		<c:set var="disabled" value="Y"/>
	</c:when>
	<c:otherwise>
		<c:set var="disabled" value="N"/>
	</c:otherwise>
</c:choose>

<div class="dialogContent userPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
<div class="popupHero">
	<h2>사용자 정보</h2>
	<p>업체 소속 사용자 정보와 권한 항목을 입력하거나 수정할 수 있습니다.</p>
</div>
<form id="formPopup">
	<input type="hidden" id="userCd" name="userCd" value="${info.userCd}"/>
	<input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>
	<ul class="section popupCard popupFormGrid popup-grid-2">
		<li class="full">
			<!-- 업체명 -->
<%-- 			<custom:popupSearchSelectBox options="${noSelect }"
					name="companyCd" label="form.company"
					searchUrl="/inside/authorization/selectCompanyCombo" id="companyCd" />
 --%>
 			<custom:popupSearchSelectBox name="companyCd" id="popCompanyCd" options="${noSelect}" searchUrl="/inside/authorization/selectCompanyCombo" label="form.companyNm" selectedValue="${info.companyCd }" selectedText="${info.companyNm }" />
		</li>

		<li>
			<custom:popupInputText id="email" name="email" label="form.email" value="${info.email }" maxlength="256"/>
		</li>
		<li>
			<custom:popupInputText id="popUserNm" name="userNm" label="form.userNm" value="${info.userNm }" maxlength="256"/>
		</li>
	</ul>
	<ul class="section popupCard popupFormGrid popup-grid-2">
		<li class="half clearfixCheck">
			<custom:popupCheckbox options="${informationProtect}" checkedValue="${info.protectYn }" name="protectYn" labelSide="left" />
<%--			<custom:popupCheckbox options="${informationCr}" checkedValue="${info.crYn }" name="crYn"  labelSide="left" />--%>
		</li>
		<li class="half clearfixCheck">
			<custom:popupCheckbox options="${informationDistribution}" checkedValue="${info.distributionYn }" name="distributionYn"  labelSide="left" />
		</li>
	</ul>
</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="saveUser()" name="approval" label="btn.save" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
