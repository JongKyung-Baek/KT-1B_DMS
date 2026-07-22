<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/outside/commonrequest/commonRequestPopup.js"></script>

<!-- 배포요청 팝업(요청번호 상세보기) -->
<script>
var gridId = 'gridRequestStatusPopup';
var formId = 'formRequestStatusPopup';
var popupGridParam;
var popupInfo = ${popupInfo}
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
	setTimeout(syncRequestStatusHeaderSpacing, 0);
	$(window).on('resize.requestStatusPopup', function() {
		syncRequestStatusHeaderSpacing();
	});
});
function setGridParam(){
	popupGridParam = {
			url: popupInfo.urlInfo + "selectRequestStatusList",
			size: 9999,
			page: 1,
			gridId : gridId,
			formId : formId,
			multiSelect : false,
			numbering : false,
			rowNum: 200,
			shrinkToFit: false
	};
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function syncRequestStatusHeaderSpacing(){
	var $grid = $('#' + gridId);
	var $view = $grid.closest('.ui-jqgrid-view');
	var $body = $view.children('.ui-jqgrid-bdiv');
	var $headerBox = $view.find('.ui-jqgrid-hbox');
	var scrollbarWidth = 0;

	if ($body.length > 0) {
		scrollbarWidth = $body[0].offsetWidth - $body[0].clientWidth;
	}

	$headerBox.css({
		'padding-right': Math.max(scrollbarWidth, 0) + 'px',
		'box-sizing': 'border-box'
	});
}

function gridComplete(){
	$('#listCount').text($('#'+gridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth();
	syncRequestStatusHeaderSpacing();
	setTimeout(syncRequestStatusHeaderSpacing, 0);
	setTimeout(syncRequestStatusHeaderSpacing, 80);
}
</script>
<div class="dialogContent commonRequestStatusPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
	<div class="popupHero">
		<h2><spring:message code="form.requestNo" /></h2>
		<p><spring:message code="btn.distributionRequest" /> 상세 정보</p>
	</div>
	<form id="formRequestStatusPopup" name="formRequestStatusPopup">
		<input type="hidden" id="objectType" name="objectType" value="${formData.objectType }"/>
		<ul class="section popupCard popupFormGrid">
			<li class="third">
				<custom:popupInputText name="requestNo" label="form.requestNo" value='${formData.requestNo}' id="requestNo" readOnly="readOnly"/>
			</li>
			<li class="third">
				<custom:popupInputText name="requestPurpose" label="form.purpose" value='${formData.requestPurposeNm}' id="requestPurpose" readOnly="readOnly"/>
			</li>
			<c:if test="${'PRODUCTION' == dataType}">
			<li class="third">
				<custom:popupInputText name="objectTypeNm" label="form.objectType" id="objectTypeNm" value='${formData.objectTypeWithP }' readOnly="readOnly"/>
			</li>
			</c:if>
			<li class="third">
				<custom:popupInputText name="businessAreaNm" label="form.businessArea" value='${formData.businessAreaNm}' id="businessAreaNm" readOnly="readOnly"/>
			</li>
			<li class="third">
				<custom:popupInputText name="purchaserNm" label="form.approvalUser" value='${formData.purchaserNm}' id="purchaserNm" readOnly="readOnly"/>
			</li>
			<li class="third">
				<custom:popupInputText name="deployUserNm" label="form.requestUser" value='${formData.deployUserNm}' id="deployUserNm" readOnly="readOnly"/>
<%-- 				<custom:popupInputText name="deployUserEmail" label="form.email" value='${formData.deployUserEmail}' id="deployUserEmail" readOnly="readOnly"/> --%>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="requestDesc" label="form.requestReason" value="${formData.requestDesc }" rows="3" id="requestDesc" disabled="Y"/>
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code='${listTitle }' /></span><span class="listCount" id="listCount"></span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id="gridRequestStatusPopup"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" id="close" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
