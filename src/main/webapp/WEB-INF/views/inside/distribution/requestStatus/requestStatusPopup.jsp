<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/requestStatus/requestStatusPopup.js"></script>
<!-- 배포승인 - 도면/문서/SW 팝업(요청번호 상세보기)-->
<script>
var popupGridParam;
var objectType = '${objectType}';
var gridId = 'gridRequestStatus';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
	if('DRAWING'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", "swVersion");
	}else if('DOC'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", "swVersion");
	}else if('SW'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", ["totalPage","page"]);
	}
});

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
}

</script>
<style>
	.section li textarea{height:80px;}
</style>
<div class="dialogContent commonRequestStatusPopup requestStatusPopup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero">
		<h2>요청 현황</h2>
	</div>
	<form id="formRequestStatusPopup">
		<input id="requestNo" name="requestNo" value="${data.requestNo}" type="hidden"/>
<%-- 	<input id="approvalLineId" name="approvalLineId" value="${approvalLineId}" type="hidden"/> --%>
<%-- 	<input id="currentProcessSeqNo" name="currentProcessSeqNo" value="${currentProcessSeqNo}" type="hidden"/> --%>
		<input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
<!-- 	<input id="requestType" name="requestType" value="PRINT" type="hidden"/> -->
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<%-- 요청사유--%>
				<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" value="${data.requestDesc }" id="requestDesc" rows="2" disabled="Y"/>
			</li>
			<li class="half">
				<%-- 반려사유  --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.rejectRequestDesc" rows="2" value="${data.rejectDesc }" id="rejectDesc" disabled="Y"/>
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 배포승인 목록 --%>
				<span class="gridTitle"><spring:message code="form.distributionApprovalList"/></span><span class="listCount" id="listCount"></span>
			</div>
			<c:if test="${data.requestType eq 'PRINT'}">
			<div class="right">
				<c:if test="${data.statusCd eq 'APPROVAL' }">
					<button class="ui-button ui-corner-all" onclick="openPrintViewer('${data.requestType}', 'gridRequestStatus', 'IN')"><spring:message code="btn.print"/></button>
				</c:if>
<%-- 				<button class="ui-button ui-corner-all" onclick="openPrint('${data.requestType}', 'gridRequestStatus', 'IN')"><spring:message code="btn.print"/></button> --%>
			</div>
			</c:if>
		</div>
		<div class="gridContainer">
			<table id="gridRequestStatus"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
