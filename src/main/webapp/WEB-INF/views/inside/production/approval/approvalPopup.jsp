<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/approval/approvalPopup.js"></script>

<!-- 배포승인 팝업(요청번호 상세보기)-->
<script>
var popupRequestInfoGridParam;
var popupObjectInfoGridParam;
var popupGridId = 'gridObjectInfoPopup';
$(function() {
	//접수자 정보 그리드
	settingGrid('${gridRequestInfo }', setPopupRequestInfoGridParam(), 'popupRequestInfoGridParam');
	//배포승인 자료 그리드
	settingGrid('${gridObjectInfo }', setPopupObjectInfoGridParam(), 'popupObjectInfoGridParam');
});
</script>
<style>
	textarea{height:80px;}
</style>
<div class="dialogContent">
	<form id="formApprovalInfoPopup">
		<input type="hidden" id="requestNo" name="requestNo" value="${requestNo }"/>
		<input type="hidden" id="objectType" name="objectType" value="${objectType }"/>
		<ul class="section">
			<li class="half">
				<%-- 요청 사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" rows="2" value="${data.requestDesc }" id="requestDesc" disabled="Y"/>
				<%-- 반려사유 --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.rejectDesc" rows="2" value="" id="rejectDesc"/>
			</li>
		</ul>
	</form>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 접수자 정보 목록 --%>
				<span class="gridTitle"><spring:message code="title.deployUserInfoList" /></span><span class="listAcceptCount"></span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id="gridRequestInfoPopup"></table>
		</div>
	</div>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 배포승인 자료 목록 --%>
				<span class="gridTitle"><spring:message code="title.distributionApprovalObjectList" /></span><span class="listCount"></span>
			</div>
<!-- 			<div class="right"> -->
<!-- 				<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
<!-- 			</div> -->
		</div>
		<div class="gridContainer">
			<table id="gridObjectInfoPopup"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 배포승인 --%>
		<custom:popupButton function="approval('A')" name="approval" label="btn.distributionApproval" id="approval"/>
		<%-- 배포반려 --%>
		<custom:popupButton function="approval('R')" name="reject" label="btn.distributionReject" id="reject"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>