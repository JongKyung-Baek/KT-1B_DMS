<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script type="text/javascript" src="/resources/js/views/outside/cr/request/requestPopup.js"></script>
<!-- CR요청 팝업(CR신규제의 버튼)-->
<script>
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
	$("#"+gridId).jqGrid('clearGridData');
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
	<div class="dialogContent">
		<form id="crRequest" name="crRequest">
		<ul class="section">
			<li class="half">
				<custom:popupSelectBox name="vendorUid" options="${deployUser }" defaultText="form.select" label="form.vendorUser" id="vendorUid"/>
<%-- 				<custom:popupInputText name="vendorEmailNm" label="form.email" value="${sessionUser.email }" id="vendorEmailNm" placeholder="form.loginUserEmail"/> --%>
				<custom:popupSelectBox name="approvalUser" options="${approvalUser }" label="form.authorizePerson" id="approvalUser" />
			</li>
			<li class="half">
				<custom:popupSelectBox name="businessAreaCd" options="${businessAreaCd }" defaultText="form.select" label="form.businessArea" id="businessAreaCd"/>
				<custom:popupSelectBox name="purchaserUid" options="${emptyArray }" defaultText="form.select" label="form.approvalUser" id="purchaserUid"/>
			</li>
		</ul>
		<ul class="section">
			<li class="half">
<%-- 				<custom:popupInputText name="crNo" label="form.suggestionNo" value="${autoIncrement}" id="crNo" disabled="disabled"/> --%>
				<label for="drawingNo"><spring:message code='form.drawingNo'/></label>
				<div><input type="text" id="drawingNo" class="rightBtn" value="" name="drawingNo">
				<button type="button" class="ui-button ui-corner-all rightBtn checkBtn" onclick="inspection()"></button></div>
				<custom:popupSelectBox name="crTypeCd" options="${crTypeCd }" defaultText="form.select" label="form.crTypeDesc" id="crTypeCd"/>
			</li>
			<li class="half">
				<custom:popupSelectBox options="${productNoList }" defaultText="form.select" selectedValue="${formData.productNo }" name="productNo" label="form.productCd" id="productNo"/>
				<custom:popupInputText name="materialNo" label="form.materialNo" value="" id="materialNo" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="drawingNm" label="form.drawingNm" value="" id="drawingNm" readOnly="readOnly"/>
				<custom:popupInputText name="revNo" label="form.revNo" value="" id="revNo" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="partNo" label="form.partNo" value="" id="partNo" readOnly="readOnly"/>
				<custom:popupInputText name="drawingInsertDt" label="form.drawingInsertDt" value="" id="drawingInsertDt" readOnly="readOnly"/>
			</li>
		</ul>
		<ul class="section">
			<li>
				<custom:popupInputText name="crTitleNm" label="form.title" value="" id="crTitleNm" />
			</li>
			<li>
				<custom:popupInputTextArea name="asIsDesc" label="form.asIsDesc" value="" rows="4" id="asIsDesc"/>
			</li>
			<li>
				<custom:popupInputTextArea name="toBeDesc" label="form.toBeDesc" value="" rows="4" id="toBeDesc"/>
			</li>
		</ul>
		</form>
		<p class="textCaution "><spring:message code="title.crRequestMsg" /><button type="button" class="ui-button ui-corner-all" onclick="crTemplateDownload()"><spring:message code="btn.crForm"/></button></p>
		<div class="section">
			<div class="dialogToolbar">
				<div class="left">
					<span class="gridTitle"><spring:message code="form.uploadFileList" /></span><span class="listCount" id="listCount"></span>
				</div>
				<div class="right">
					<form id="crFileForm" name="crFileForm" enctype="multipart/form-data">
						<input type="file" id="crFile" name="crFile" multiple="multiple" onchange="fileAppendEvent()" style="display: none;"/>
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
		<button class="ui-button ui-corner-all bottomBtn" onclick="requestCr()"><spring:message code="btn.crRequest" /></button>
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>