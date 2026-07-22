<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/outside/cr/request/requestStatusPopup.js"></script>
<!-- CR 접수 팝업(제의번호 상세보기)-->
<script>
var info = '${popupInfo}';
$(document).ready(function(){
	//console.log(${popupInfo});
});
</script>
<!-- <script type="text/javascript" src="/resources/js/views/inside/cr/commonInsideCrPopup.js"></script> -->
<div class="dialogContent requestStatusPopup popup-base popup-actions-center popup-type-detail">
	<div class="popupHero">
		<h2><spring:message code="title.crRequest" text="CR 요청" /></h2>
		<p><spring:message code="title.requestStatus" text="요청 상태와 상세 정보를 확인할 수 있습니다." /></p>
	</div>
	<form id="crForm" name="crForm">
		<input type="hidden" id="crNo" name="crNo" value="${data.crNo }">
		<ul class="section popupCard popupFormGrid popup-grid-1">
			<li>
			<%-- 현재상태 --%>
<%-- 			<custom:popupInputText name="statusNm" label="form.vendorUser" id="statusNm" value="${data.statusNm }" disabled="disabled"/> --%>
			<%-- 반려사유 --%>
				<custom:popupInputTextArea name="rejectDesc" label="form.rejectDesc" rows="2" value="${data.rejectDesc }" id="rejectDesc" disabled="${rejectDisabledYn }"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-4">
			<li>
				<%-- 업체담당자 --%>
				<custom:popupInputText name="vendorUid" label="form.vendorUser" id="vendorUid" value="${data.vendorUserNm }" disabled="disabled"/>
			</li>
			<li>
				<%-- 업체결재자 --%>
				<custom:popupInputText name="approvalUser" label="form.companyAuthorizePerson" id="approvalUser" value="${data.approvalUser }" disabled="disabled"/>
			</li>
			<li>
				<%-- 사업장 --%>
				<custom:popupInputText name="businessAreaCd" label="form.businessArea" id="businessAreaCd" value="${data.businessAreaNm }" disabled="disabled"/>
			</li>
			<li>
				<%-- 구매 담당자 --%>
				<custom:popupInputText name="purchaserUid" label="form.approvalUser" id="purchaserUid" value="${data.purchaserUserNm }" disabled="disabled"/>
			</li>
			<li>
				<%-- 도면번호 --%>
				<custom:popupInputText name="drawingNo" label="form.drawingNo" id="drawingNo" value="${data.drawingNo }" disabled="disabled"/>
			</li>
			<li>
				<%-- 제의사유 --%>
				<custom:popupInputText name="crTypeCd" value="${data.crTypeCd }" label="form.crTypeDesc" id="crTypeCd" disabled="disabled"/>
			</li>
			<li>
				<%-- 기종 --%>
				<custom:popupInputText value="${data.productNm }" name="productNo" label="form.productCd" id="productNo" disabled="disabled"/>
			</li>
			<li>
				<%-- 자재코드 --%>
				<custom:popupInputText name="materialNo" label="form.materialNo" value="${data.materialNo }" id="materialNo" disabled="disabled"/>
			</li>
			<li>
				<%-- 도명 --%>
				<custom:popupInputText name="drawingNm" label="form.drawingNm" value="${data.drawingNm }" id="drawingNm" disabled="disabled"/>
			</li>
			<li>
				<%-- REV --%>
				<custom:popupInputText name="revNo" label="form.revNo" value="${data.revNo }" id="revNo" disabled="disabled"/>
			</li>
			<li>
				<%-- 품번 --%>
				<custom:popupInputText name="partNo" label="form.partNo" id="partNo" disabled="disabled" value="${data.partNo }"/>
			</li>
			<li>
				<%-- 도면 등록일 --%>
				<custom:popupInputText name="drawingInsertDt" label="form.drawingInsertDt" value="${data.insertDt }" id="drawingInsertDt" disabled="disabled"/>
			</li>
			<li class="full">
				<%-- 장치명 --%>
				<custom:popupInputText name="deviceNm" label="form.deviceNm" value="${data.deviceNm }" id="deviceNm" disabled="disabled"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-1">
			<li>
				<custom:popupInputText name="crTitleNm" label="form.title" value="${data.crTitleNm }" id="crTitleNm" readOnly="readOnly"/>
			</li>
			<li>
				<custom:popupInputTextArea name="asIsDesc" label="form.asIsDesc" value="${data.asIsDesc }" rows="4" id="asIsDesc" disabled="Y"/>
			</li>
			<li>
				<custom:popupInputTextArea name="toBeDesc" label="form.toBeDesc" value="${data.toBeDesc }" rows="4" id="toBeDesc" disabled="Y"/>
			</li>
			<li>
				<label for=""><spring:message code="form.uploadFileList" /><span class="listCount" id="listCount">${fn:length(formData.fileList)}</span></label>
				<div class="fileDownloadList">
					<c:forEach items="${formData.fileList }" var="fileInfo">
						<div class="fileDownload">
							<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( "${fileInfo.crNo}", ${fileInfo.fileNo })' ><span>${fileInfo.orgFileNm }</span></button>
						</div>
					</c:forEach>
				</div>
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
	<c:choose>
		<c:when test="${popupInfo.equals('APPROVAL')}">
			<button class="ui-button ui-corner-all bottomBtn" onclick="approve()"><spring:message code='btn.crApprove' /></button>
			<button class="ui-button ui-corner-all bottomBtn" onclick="approvalReject()"><spring:message code='btn.reject' /></button>
		</c:when>
	</c:choose>
		<button id="close" name="close" class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>
