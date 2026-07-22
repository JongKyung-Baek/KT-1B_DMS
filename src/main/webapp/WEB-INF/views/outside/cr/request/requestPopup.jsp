<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script type="text/javascript" src="/resources/js/views/outside/cr/request/requestPopup.js"></script>
<!-- CR요청 팝업(CR 제기회의 버튼) -->
<script>
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
	$("#"+gridId).jqGrid('clearGridData');
	
	//$('#vendorUid').attr('disabled','true');
});

function setGridParam(){
	popupGridParam = {
			gridId : gridId,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check',
			cellEdit: true,
			cellsubmit: 'clientArray'
	};
}
</script>
		<div class="dialogContent commonRequestPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible outsideCrRequestPopup">
		<div class="popupHero">
			<h2><spring:message code="title.crRequest" text="CR 요청" /></h2>
			<p><spring:message code="title.crRequestInfo" text="요청 정보를 입력한 뒤 CR 요청을 진행해 주세요." /></p>
		</div>
		<form id="crRequest" name="crRequest">
		<input type="hidden" id="vendorEmailNm" name="vendorEmailNm" value="${sessionUser.email }">
		<input type="hidden" id="vendorUidNm" name="vendorUidNm" value="${sessionUser.userNm }">
		
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li>
				<custom:popupSelectBox name="vendorUid" options="${deployUser }" selectedValue="${sessionUser.userId }" defaultText="form.select" label="form.vendorUser" id="vendorUid" />
			</li>
			<li>
				<custom:popupSelectBox name="approvalUser" options="${approvalUser }" selectedValue="${sessionUser.companyApprovalUser}" label="form.companyAuthorizePerson" id="approvalUser" />
			</li>
			<li>
				<custom:popupSelectBox name="businessAreaCd" options="${businessAreaCd }" defaultText="form.select" label="form.businessArea" id="businessAreaCd"/>
			</li>
			<li>
				<custom:popupSelectBox name="purchaserUid" options="${emptyArray }" defaultText="form.select" label="form.approvalUser" id="purchaserUid"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<custom:popupSelectBox options="${productNoList }" defaultText="form.select" selectedValue="${formData.productNo }" name="productNo" label="form.productCd" id="productNo"/>
				<custom:popupSelectBox name="crTypeCd" options="${crTypeCd }" defaultText="form.select" label="form.crTypeDesc" id="crTypeCd"/>
			</li>
			<li class="half">
<%-- 				<custom:popupInputText name="crNo" label="form.suggestionNo" value="${autoIncrement}" id="crNo" disabled="disabled"/> --%>
				<label for="partNo"><spring:message code='form.partNo'/></label>
				<div><input type="text" id="partNo" class="rightBtn" value="" name="partNo">
				<button type="button" class="ui-button ui-corner-all rightBtn checkBtn" onclick="inspection()"></button></div>
				<custom:popupInputText name="partNm" label="form.partNm" value="" id="partNm" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="drawingNo" label="form.drawingNo" value="" id="drawingNo" />
				<custom:popupInputText name="revNo" label="form.revNo" value="" id="revNo"/>
			</li>
			<li class="half">
				<custom:popupInputText name="drawingNm" label="form.drawingNm" value="" id="drawingNm" />
				<custom:popupInputText name="materialNo" label="form.materialNo" value="" id="materialNo" readOnly="readOnly"/>
			</li>
			<li class="full">
				<%-- 장치명 --%>
				<custom:popupInputText name="deviceNm" label="form.deviceNm1" value="" id="deviceNm"/>
				<custom:popupInputText name="stdPartNo" label="form.stdPartNo" value="" id="stdPartNo" readOnly="readOnly"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-1">
			<li class="full">
				<custom:popupInputText name="crTitleNm" label="form.title" value="" id="crTitleNm" />
			</li>
			<li class="full">
				<custom:popupInputTextArea name="asIsDesc" label="form.asIsDesc" value="" rows="4" id="asIsDesc"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="toBeDesc" label="form.toBeDesc" value="" rows="4" id="toBeDesc"/>
			</li>
		</ul>
		</form>
		<p class="textCaution "><spring:message code="title.crRequestMsg" /><button type="button" class="ui-button ui-corner-all" onclick="crTemplateDownload()"><spring:message code="btn.crForm"/></button></p>
		<div class="section popupCard">
			<div class="dialogToolbar">
				<div class="left">
					<span class="gridTitle"><spring:message code="form.uploadFileList" /></span><span class="listCount" id="listCount"></span>
					<p class="textCaution "><spring:message code="msg.plzUploadCrFile"/></p>
				</div>
				<div class="right">
					<form id="crFileForm" name="crFileForm" enctype="multipart/form-data" style="display: none;">
						<input type="file" id="crFile" name="crFile" multiple="multiple" onchange="fileAppendEvent()"/>
					</form>
					<button class="ui-button ui-corner-all" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
					<button class="ui-button ui-corner-all" onclick="fileDelete()"><spring:message code="btn.fileDelete"/></button>
				</div>
			</div>
			<div class="gridContainer">
				<table id="gridCrRequestUploadPopup"></table>
			</div>
		</div>
	</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button id="req" class="ui-button ui-corner-all bottomBtn" onclick="requestCr()"><spring:message code="btn.crRequest" /></button>
		<button id="close" name="close" class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>

