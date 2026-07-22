<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
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
		<div class="dialogContent commonRequestStatusPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
			<div class="popupHero">
				<h2><spring:message code="form.requestNo" /></h2>
				<p><spring:message code="form.userRequestType" /> 상세 정보</p>
			</div>
			<form id="request" name="request">
			<input id="requestNo" name="requestNo" value="${DetailInfo.requestNo}" type="hidden"/>
			<c:if test="${requestNo == ''}">
			<input id="requestType" name="requestType" value="I" type="hidden" disabled="disabled"/>
   			</c:if>
				<ul class="section popupCard popupFormGrid">
					<li class="third">
						<custom:popupInputText name="companyNm" label="form.companyNm" value="${sessionUser.companyNm }" id="companyNm" readOnly="readOnly"/>
					</li>
				<li class="third">
				<custom:popupInputText name="userNm" label="grid.userNm" value="${DetailInfo.userNm}" id="userNm" disabled="disabled"/>
			</li>
				<li class="third">
					<custom:popupInputText name="email" label="form.email" value="${DetailInfo.email}" id="email" disabled="disabled"/>
				</li>
				<c:if test="${requestNo != ''}">
				<li class="third">
					<custom:popupSelectBox label="form.userRequestType" options="${userRequestType}" selectedValue="${DetailInfo.requestType }" id="requestType" name="requestType" disabled="Y"/>
				</li>
	   			</c:if>
			</ul>
				<ul class="section popupCard popupFormGrid">
					<li class="third clearfixCheck permissionCheckField">
						<custom:popupCheckbox options="${informationProtect}" checkedValue="${DetailInfo.protectYn }" name="protectYn" labelSide="left" disabled="disabled"/>
					</li>
					<li class="third clearfixCheck permissionCheckField">
						<custom:popupCheckbox options="${informationCr}" checkedValue="${DetailInfo.crYn }" name="crYn" labelSide="left" disabled="disabled"/>
					</li>
					<li class="third">
						<custom:popupInputText value="${DetailInfo.aprrovalUserNm }" name="approvalUserNm" label="form.authorizePerson" id="approvalUserNm" readOnly="readOnly"/>
				</li>
				<li class="full">
					<custom:popupInputTextArea name="requestReason" label="form.requestReason" rows="2" value="${DetailInfo.requestReason}" id="requestReason" disabled="Y"/>
				</li>
			</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" id="close" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>
