<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/approval/approvalPopup.js"></script>
<!-- 배포승인 - 도면/문서/SW 팝업(요청번호 상세보기) -->
<script>
var popupGridParam;
var popupGridId = 'gridApproval';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam());
});

function formatAddMonth(cellValue, options, rowdata, action){
	if(undefined == cellValue) {
		return '';
	}else {
		return cellValue + g_msg("msg.month");
	}
}

</script>
<div class="dialogContent commonRequestPopup distributionApprovalPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2 style="padding-top: 24px;">배포 승인</h2>
		<p>요청 정보를 확인하고 승인 또는 반려를 진행해 주세요.</p>
	</div>
	<form id="formApprovalPopup">
		<input id="requestNo" name="requestNo" value="${requestNo}" type="hidden"/>
		<input id="approvalLineId" name="approvalLineId" value="${approvalLineId}" type="hidden"/>
		<input id="currentProcessSeqNo" name="currentProcessSeqNo" value="${currentProcessSeqNo}" type="hidden"/>
		<input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
		<input id="requestType" name="requestType" value="DISTRIBUTION" type="hidden"/>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<%-- 요청사유 --%>
				<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" value="${requestDesc }" id="requestDesc" rows="2" disabled="Y"/>
			</li>
			<li class="half">
				<%-- 반려사유 --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.rejectRequestDesc" rows="2" value="" id="rejectDesc"/>
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 배포승인 목록 --%>
				<span class="gridTitle"><spring:message code="form.distributionApprovalList"/></span><span class="listCount" id="listCount"></span>
			</div>
<!-- 		<div class="right"> -->
<!-- 			<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
<!-- 		</div> -->
		</div>
		<div class="gridContainer">
			<table id="gridApproval"></table>
		</div>
	</div>
	<div class="popupOptionRow">
<%--		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>--%>
		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y" checked onclick="toggleCheckboxValue('emailCheck')">
		<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
		<script>
		$("#emailCheck").prettyCheckable({labelPosition: 'left'});
		</script>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<%-- 배포 승인 --%>
			<custom:popupButton function="save('A')" name="approval" label="btn.distributionApproval" id="approval"/>
			<%-- 배포 반려 --%>
			<custom:popupButton function="save('R')" name="reject" label="btn.distributionReject" id="reject"/>
			<%-- 닫기 --%>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
</div>
