<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/outsideuser/companyPopup.js"></script>

<c:choose>
	<c:when test="${saveFlag == 'U' }">
		<c:set var="disabled" value="Y"/>
	</c:when>
	<c:otherwise>
		<c:set var="disabled" value="N"/>
	</c:otherwise>
</c:choose>
<style>
	.ui-dialog .ui-dialog-content{overflow:inherit;}
</style>
<div class="dialogContent companyPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
<div class="popupHero">
	<h2>업체 정보</h2>
	<p>업체 기본 정보와 배포 구매담당자를 입력하거나 수정할 수 있습니다.</p>
</div>
<form id="formPopup">
	<input type="hidden" id="companyCd" name="companyCd" value="${info.companyCd}"/>
	<input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>
	<ul class="section popupCard popupFormGrid popup-grid-2">
		<li>
			<!-- 업체명 -->
			<custom:popupInputText name="companyNm" id="companyNm" label="form.companyNm" value="${info.companyNm }" disabled="${disabled }"/>
			<!-- 사업자번호 -->
		</li>
		<li>
			<custom:popupInputText id="bizNo" name="bizNo" label="form.companyCd" value="${info.bizNo }" maxlength="10"/>
		</li>
	</ul>
	<ul class="section popupCard popupFormGrid popup-grid-1">
		<li class="full">
			<!-- 배포구매담당자1 -->
			<custom:popupSelectInputBox options="${purchaser1 }" name="distPurchaserUserCd1" label="form.distPurchaserUserCd1" id="distPurchaserUserCd1" selectedValue="${info.distPurchaserUserCd1 }"/>
		</li>
	</ul>
<%--	<ul class="section">--%>
<%--		<li class="enlargeLabel">--%>
<%--			<!-- cr구매담당자1 -->--%>
<%--			<custom:popupSelectInputBox options="${purchaser1 }" name="crPurchaserUserCd1" label="form.crPurchaserUserCd1" id="crPurchaserUserCd1" selectedValue="${info.crPurchaserUserCd1 }"/>--%>
<%--		</li>--%>
<%--	</ul>--%>
<%--	<ul class="section">--%>
<%--		<li class="enlargeLabel">--%>
<%--			<!-- 배포구매담당자1 -->--%>
<%--			<custom:popupSelectInputBox options="${purchaser2 }" name="distPurchaserUserCd2" label="form.distPurchaserUserCd2" id="distPurchaserUserCd2" selectedValue="${info.distPurchaserUserCd2 }"/>--%>
<%--		</li>--%>
<%--	</ul>--%>
<%--	<ul class="section">--%>
<%--		<li class="enlargeLabel">--%>
<%--			<!-- cr구매담당자1 -->--%>
<%--			<custom:popupSelectInputBox options="${purchaser2 }" name="crPurchaserUserCd2" label="form.crPurchaserUserCd2" id="crPurchaserUserCd2" selectedValue="${info.crPurchaserUserCd2 }"/>--%>

<%--		</li>--%>
<%--	</ul>--%>
</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="saveCompany()" name="approval" label="btn.save" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
