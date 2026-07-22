<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<sec:authentication property="principal" var="sessionUser" />
<!-- CR 접수 팝업(제의번호 상세보기)-->
<script>
var info = '${popupInfo}';
$(document).ready(function(){
	//console.log(${popupInfo});
});
</script>
<script type="text/javascript" src="/resources/js/views/inside/cr/commonInsideCrPopup.js"></script>
<div class="dialogContent">
	<form id="crForm" name="crForm">
		<input type="hidden" id="crNo" name="crNo" value="${formData.crNo }">
		<ul class="section">
			<li class="half">
				<c:choose>
					<c:when test="${popupInfo.equals('ACCEPTANCE')}">
						<custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${teamLeaderUid }" name="approvalUser" label="form.authorizePerson" id="approvalUser"/>
					</c:when>
					<c:when test="${popupInfo.equals('APPROVAL')}">
						<custom:popupInputText name="approvalUser" label="form.authorizePerson" value="${sessionUser.userNm }" id="approvalUser" readOnly="readOnly"/>
					</c:when>
					<c:otherwise>
						<custom:popupInputText name="approvalUser" label="form.authorizePerson" value="${formData.approvalUser }" id="approvalUser" readOnly="readOnly"/>
					</c:otherwise>
				</c:choose>
				<%-- 결재자 --%>
			</li>
			<c:choose>
				<c:when test="${popupInfo.equals('ACCEPTANCE')}">
					<li class="half">
						<custom:popupInputTextArea name="reviewResult" label="form.reviewComment" value="" rows="4" id="reviewResult"/>
						<custom:popupInputTextArea name="rejectReason" label="form.rejectDesc" value="" rows="4" id="rejectReason"/>
					</li>
				</c:when>
				<c:when test="${popupInfo.equals('APPROVAL')}">
					<li class="half">
						<custom:popupInputTextArea name="reviewResult" label="form.reviewComment" value="${formData.reviewResult }" rows="4" id="reviewResult" disabled="Y"/>
						<custom:popupInputTextArea name="rejectReason" label="form.rejectDesc" value="" rows="4" id="rejectReason" />
					</li>
				</c:when>
				<c:otherwise>
					<li class="half">
						<custom:popupInputTextArea name="reviewResult" label="form.reviewComment" value="${formData.reviewResult }" rows="4" id="reviewResult" disabled="Y"/>
						<custom:popupInputTextArea name="rejectReason" label="form.rejectDesc" value="${formData.rejectReason }" rows="4" id="rejectReason" disabled="Y"/>
					</li>
				</c:otherwise>
			</c:choose>
		</ul>
		<ul class="section">
			<c:choose>
				<c:when test="${popupInfo.equals('ACCEPTANCE')}">
					<li class="half">
						<custom:popupSelectBox options="${productNoList }" defaultText="form.select" selectedValue="${formData.productNo }" name="productNo" label="form.productCd" id="productNo"/>
<%-- 						<custom:popupInputText name="materialNo" label="form.materialNo" value="${formData.materialNo }" id="materialNo" /> --%>
						<%-- 장치명 --%>
						<custom:popupInputText name="deviceNm" id="deviceNm" label="form.deviceNm" value="${formData.deviceNm }" disabled="disabled" />
					</li>
				</c:when>
				<c:otherwise>
					<li class="half">
						<custom:popupInputText name="productNo" label="form.productCd" value="${formData.productCd }" id="productNo" readOnly="readOnly"/>
<%-- 						<custom:popupInputText name="materialNo" label="form.materialNo" value="${formData.materialNo }" id="materialNo" readOnly="readOnly"/> --%>
						<%-- 장치명 --%>
						<custom:popupInputText name="deviceNm" id="deviceNm" label="form.deviceNm" value="${formData.deviceNm }" disabled="disabled" />
					</li>
				</c:otherwise>
			</c:choose>
			<li class="half">
				<custom:popupInputText name="drawingNo" label="form.drawingNo" value="${formData.drawingNo }" id="drawingNo" readOnly="readOnly"/>
				<custom:popupInputText name="revNo" label="form.revNo" value="${formData.revNo }" id="revNo" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="partNo" label="form.partNo" value="${formData.partNo }" id="partNo" readOnly="readOnly"/>
				<custom:popupInputText name="crTypeDesc" label="form.crTypeDesc" value="${formData.crTypeDesc }" id="crTypeDesc" readOnly="readOnly"/>
			</li>
		</ul>
		<ul class="section">
			<li>
				<custom:popupInputText name="crTitleNm" label="form.title" value="${formData.crTitleNm }" id="crTitleNm" readOnly="readOnly"/>
			</li>
			<li>
				<custom:popupInputTextArea name="asIsDesc" label="form.asIsDesc" value="${formData.asIsDesc }" rows="4" id="asIsDesc" disabled="Y"/>
			</li>
			<li>
				<custom:popupInputTextArea name="toBeDesc" label="form.toBeDesc" value="${formData.toBeDesc }" rows="4" id="toBeDesc" disabled="Y"/>
			</li>
			<li>
				<label for=""><spring:message code="form.uploadFileList" /><span class="listCount" id="listCount">${fn:length(formData.fileList)}</span></label>
				<div class="fileDownload"><c:forEach items="${formData.fileList }" var="fileInfo"><button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( ${fileInfo.cnSerial}, ${fileInfo.fileNo }, "${fileInfo.orgFileNm }")' ><span>${fileInfo.orgFileNm }</span></button></c:forEach></div>
			</li>
		</ul>
		<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left">
	<c:if test="${popupInfo.equals('ACCEPTANCE')}">
		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>
		<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
		<script>
		$("#emailCheck").prettyCheckable({labelPosition: 'left'});
		</script>
	</c:if>
	</div>
	<div class="right">
		<c:choose>
			<c:when test="${popupInfo.equals('ACCEPTANCE') && formData.purchaserUid == sessionUser.userId}">
				<button class="ui-button ui-corner-all bottomBtn" onclick="approvalRequest()"><spring:message code='btn.crApprovalRequest' /></button>
				<button class="ui-button ui-corner-all bottomBtn" onclick="acceptanceReject()"><spring:message code='btn.reject' /></button>
			</c:when>
			<c:when test="${popupInfo.equals('APPROVAL')}">
				<button class="ui-button ui-corner-all bottomBtn" onclick="approve()"><spring:message code='btn.crApprove' /></button>
				<button class="ui-button ui-corner-all bottomBtn" onclick="approvalReject()"><spring:message code='btn.reject' /></button>
			</c:when>
			<c:otherwise>
			</c:otherwise>
		</c:choose>
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>
