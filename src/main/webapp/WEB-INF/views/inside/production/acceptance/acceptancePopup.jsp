<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/acceptance/acceptancePopup.js"></script>
<!-- 현장관리자 접수 팝업(접수 버튼)-->
<script>
var popupGridParam;
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});
</script>
<style>
	.section li textarea{height:80px;}
</style>
<div class="dialogContent">
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 접수 자료 목록 --%>
				<span class="gridTitle"><spring:message code="title.acceptanceDataList"/></span><span class="listCount" id="listCount"></span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id="gridAcceptancePopup"></table>
		</div>
	</div>
	<form id="formAcceptancePopup">
		<input type="hidden" id="requestNo" name="requestNo" value="${requestNo }"/>
		<input type="hidden" id="objectType" name="objectType" value="${objectType }"/>
		<ul class="section">
			<li>
				<%-- 배포 확인 및 전달 교육 결과 --%>
				<p class="sectTitle"><spring:message code="title.distributionConfirmAndEduResult"/></p>
			</li>
			<li class="half">
				<%-- 교육 일자 --%>
				<custom:popupCalendar name="deployDt" label="form.eduDate" defaultDate="today" id="deployDt" minDate="today"/>
	<%-- 			<custom:popupInputText name="deployDt" id="deployDt" label="form.eduDate" value=""/> --%>
				<%-- 교육 대상 --%>
				<custom:popupInputText name="deployTarget" id="deployTarget" label="form.eduTarget" value=""/>
			</li>
			<li>
				<%-- 교육 결과 --%>
				<custom:popupInputText name="deployResult" id="deployResult" label="form.eduResult" value="" />
			</li>
			<li>
				<%-- 폐기내용 --%>
				<custom:popupInputTextArea name="destroyInfo" id="destroyInfo" label="form.destroyInfo" value="" rows="2"/>
				<!-- <custom:popupInputTextArea name="deployResult" id="deployResult" label="form.eduResult" value="" rows="2"/> -->
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 접수완료 --%>
		<custom:popupButton function="acceptance()" name="accept" label="btn.acceptComplete" id="accept"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
