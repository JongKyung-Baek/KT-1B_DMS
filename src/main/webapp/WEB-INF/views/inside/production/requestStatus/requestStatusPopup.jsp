<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>

<script type="text/javascript" src="/resources/js/views/inside/production/requeststatus/requestStatusPopup.js"></script>
<!-- 검색 및 배포요청 팝업(생산기술자료 배포 승인요청 버튼)-->
<script>
var gridAcceptanceId = 'gridProductionRequestAcceptance';
var gridProductionId = 'gridProductionRequestProduction';

var gridAcceptanceParam;
var gridProductionParam;
$(document).ready(function (){
	setGridAcceptanceParam();
	setGridProductionParam();
	settingGrid('${gridAcceptance}', gridAcceptanceParam, 'gridAcceptanceParam');
	settingGrid('${gridProduction}', gridProductionParam, 'gridProductionParam');

});

function formatOpenFileList(cellValue, options, rowdata, action){
	if(undefined === cellValue) {
		return '';
	}else{
		return '<a onclick="openFileList(\'' + rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["objectTypeData"] + '\')">'+cellValue+'</a>';
	}
}
function openFileList(requestNo, objectId, objectTypeData) {
	openDialogPopup("/common/viewer/openFileListPopup", {objectId : objectId, requestNo: requestNo, objectType : objectTypeData, actionCd: $('#actionCd').val()}, "viewerDialog", 's', 470);
}

</script>
<div class="dialogContent">
	<form id="formApprovalInfoPopup" name="formApprovalInfoPopup">
	<input type="hidden" id="requestNo" name="requestNo" value="${requestNo }" />
	<input type="hidden" id="objectType" name="objectType" value="${objectType }" />
	<input type="hidden" id="actionCd" name="actionCd" value="${data.actionCd }" />
	<ul class="section">
		<li class="half">
			<input type="hidden" name="requestUser" id="requestUser" value="sessionUser.userCd" />
			<custom:popupInputText name="requestUserNm" label="form.requestUserName" value="${data.userNm }" id="requestUserNm" disabled="disabled"/>
			<%-- 결재자 --%>
			<custom:popupInputText name="teamLeader"  value="${data.approvalUserNm }"  label="form.authorizePerson" id="teamLeader" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputText name="deployDate" label="form.deployDate" id="deployDate" value="${data.deployDate }" disabled="disabled"/>
			<custom:popupInputText name="deployTerm" label="form.deployDateTerm" id="deployTerm" value="${data.deployTerm }" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputText name="deployType" label="form.deployType" id="deployDate" value="${data.deployType }" disabled="disabled"/>
			<label for=""><spring:message code="form.watermarkPrintYn"/></label>
			<custom:popupRadio labelSide="right" options="${watermarkYn }" name="watermarkYn" checkedValue="${data.watermarkYn }" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputText name="statusNm" label="form.approvalStatus" id="statusNm" value="${data.statusNm }" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputTextArea name="requestReason" label="form.requestReason" rows="4" value="${data.requestReason}" id="requestReason" disabled="Y"/>
			<custom:popupInputTextArea name="rejectReason" label="form.rejectDesc" rows="4" value="${data.rejectReason}" id="rejectReason" disabled="Y"/>
		</li>
	</ul>
	</form>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code='form.deployAcceptanceList'/></span><span class="listCount" id="acceptanceListCount"></span>
<%-- 				<span class="gridTitle"><spring:message code='form.deployAcceptanceList'/></span><span class="listCount" id="acceptanceListCount">${acceptanceListCount }</span> --%>
			</div>
<!-- 			<div class="right"> -->
<%-- 				<button class="ui-button ui-corner-all" onclick='addRow()'><spring:message code='toolbar.add'/></button> --%>
<%-- 				<button class="ui-button ui-corner-all" onclick='deleteRow()'><spring:message code='toolbar.delete'/></button> --%>
<!-- 			</div> -->
		</div>
		<%-- 접수자 리스트 --%>
		<div class="gridContainer">
			<table id="gridProductionRequestAcceptance"></table>
		</div>
	</div>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code='form.deployDocumentList'/></span><span class="listCount" id="productionListCount"></span>
			</div>
			<div class="right"></div>
		</div>
		<%-- 아이템 리스트 --%>
		<div class="gridContainer">
			<table id="gridProductionRequestProduction"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
<%-- 		<button class="ui-button ui-corner-all bottomBtn" onclick='deployRequest()'><spring:message code='btn.productionRequest'/></button> --%>
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code='btn.close'/></button>
	</div>
</div>