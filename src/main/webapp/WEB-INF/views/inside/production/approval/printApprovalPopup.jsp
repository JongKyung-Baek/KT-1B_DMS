<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/approval/printApprovalPopup.js"></script>
<!-- 배포승인 - 도면/문서/SW 팝업(요청번호 상세보기)-->
<script>
var popupGridParam;
$(function() {
	settingGrid('${gridInfo}', setPopupGridParam(), 'popupGridParam');
});
function formatOpenFileList(cellValue, options, rowdata, action){
	if(undefined === cellValue) {
		return '';
	}else{
		return '<a onclick="openFileList(\'' + rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["objectTypeData"] + '\')">'+cellValue+'</a>';
	}
}
</script>
<style>
	textarea{height:80px;}
</style>
<div class="dialogContent">
	<form id="formPrintApprovalPopup">
		<input id="requestNo" name="requestNo" value="${data.requestNo}" type="hidden"/>
		<input id="objectType" name="objectType" value="${data.objectType}" type="hidden"/>
		<ul class="section">
			<li class="half">
				<%-- 요청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" value="${data.requestDesc }" id="requestDesc" rows="2" disabled="Y"/>
				<%-- 반려사유  --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.printRejectDesc" rows="2" value="${data.rejectDesc }" id="rejectDesc"/>
			</li>
		</ul>
	</form>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 출력 승인 목록 --%>
				<span class="gridTitle"><spring:message code="title.printApprovalList"/></span><span class="listCount" id="listCount"></span>
			</div>
<!-- 		<div class="right"> -->
<!-- 			<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
<!-- 		</div> -->
		</div>
		<div class="gridContainer">
			<table id="gridPrintApprovalPopup"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 출력승인 --%>
		<custom:popupButton function="approval('A')" name="approval" label="btn.printApproval" id="approval"/>
		<%-- 출력반려--%>
		<custom:popupButton function="approval('R')" name="reject" label="btn.printReject" id="reject"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>