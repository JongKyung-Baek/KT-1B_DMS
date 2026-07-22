<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/unregisted/requeststatus/requestStatusPopup.js"></script>
<!-- 배포요청 현황 팝업(요청번호 상세보기) -->
<script>
var popupGridParam;
var popupGridId="gridRequestStatusPopup";

var printYn = "${data.printYn}";
var deployNormalYn = "${data.deployNormalYn}";
var deploySpecialYn = "${data.deploySpecialYn}";

$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'UNREG_DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'1\')">'+cellValue+'</a>';
}
</script>
<style>
	div.section{margin-bottom:10px;}
</style>
<div class="dialogContent">
<%-- 	<input type="hidden" id="printYn" name="printYn" value="${data.printYn }"> --%>
<%-- 	<input type="hidden" id="deployNormalYn" name="deployNormalYn" value="${data.deployNormalYn }"> --%>
<%-- 	<input type="hidden" id="deploySpecialYn" name="deploySpecialYn" value="${data.deploySpecialYn }"> --%>
	<ul class="section">
		<li class="half">
			<%-- 업체 --%>
			<custom:popupInputText name="companyNm" id="companyNm" label="form.company" value="${data.deployCompanyNm }" disabled="disabled"/>
			<%-- 업체 사용자 --%>
			<custom:popupInputText name="deployUserNm" id="deployUserNm" label="form.companyUser" value="${data.deployUserNm }" disabled="disabled"/>
		</li>
			<%-- email --%>
<%-- 			<custom:popupInputText name="email" id="email" label="form.email" value="${data.deployEmail }" disabled="disabled"/> --%>
		<li class="half">
			<%-- 구매담당자 --%>
			<custom:popupInputText name="purchaseUserNm" id="purchaseUserNm" label="form.approvalUser" value="${data.purchaserUserNm }" disabled="disabled"/>
			<%-- 결재자 --%>
			<custom:popupInputText name="approvalUserNm" id="approvalUserNm" label="form.authorizePerson" value="${data.teamLeader }" disabled="disabled"/>
		</li>
		<li>
			<custom:popupMultiSelectBox disabled="Y" count="8" options="${securityUserList}" selectedValue="${deployEmailCC}" name="securityUserList" label="form.securityUserList" id="securityUserList" />
		</li>
	</ul>
	<ul class="section">
		<li class="distributionCheck">
			<!--  배포방식 -->
			<label for=""><spring:message code="form.distributionType"/></label>
			<div>
				<custom:popupRadio labelSide="right" options="${distributionMethod }" name="distributionType"/>
				<div class="fileDistributionType">
					<custom:popupRadio labelSide="right" options="${distributionFileType }" name="fileDistributionType" disabled="disabled" />
				</div>
			</div>
		</li>
		<li class="half">
			<%-- 용도 --%>
			<custom:popupInputText name="requestPurpose" id="requestPurpose" label="form.purpose" value="${data.requestPurpose }" disabled="disabled"/>
			<%-- 유효기간 --%>
			<custom:popupInputText name="deployTerm" id="deployTerm" label="form.deployTerm" value="${data.deployTerm }" disabled="disabled"/>
		</li>
		<li class="half">
			<%-- 요청사유 --%>
			<custom:popupInputTextArea name="requestDesc" label="form.requestReason" rows="2" value="${data.requestDesc }" id="requestDesc" disabled="Y"/>
			<%-- 반려사유 --%>
			<custom:popupInputTextArea name="rejectDesc" label="form.rejectDesc" rows="2" value="${data.rejectDesc }" id="rejectDesc" disabled="Y"/>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 배포요청목록 --%>
				<span class="gridTitle"><spring:message code="title.distributionApprovalList"/></span><span class="listCount" id="listCount"></span>
			</div>
<!-- 			<div class="right"> -->
<!-- 				<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
<!-- 			</div> -->
		</div>
		<div class="gridContainer">
			<table id="gridRequestStatusPopup"></table>
		</div>
	</div>
	<form id="formRequestStatusPopup">
		<input type="hidden" id="requestNo" name="requestNo" value="${requestNo }"/>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>