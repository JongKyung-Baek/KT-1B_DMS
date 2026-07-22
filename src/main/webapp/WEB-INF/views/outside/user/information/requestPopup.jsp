<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script type="text/javascript" src="/resources/js/views/outside/user/information/requestPopup.js"></script>
<!-- CR요청 팝업(CR신규제의 버튼)-->
<script>
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
	$("#"+gridId).jqGrid('clearGridData');

	$("#protectYn").change(function(){
		<c:if test="${DetailInfo.protectYn !='Y' }">
		if ($("#protectYn").is(":checked")) {
			var param;
			callAjax(param, "/outside/user/information/selectProtectCount", selectProtectCountCallback);
		}
		</c:if>
	  });

	$("#crYn").change(function(){
		<c:if test="${DetailInfo.crYn !='Y' }">
		if ($("#crYn").is(":checked")) {
			var param;
			callAjax(param, "/outside/user/information/selectCrCount", selectCrCountCallback);
		}
		</c:if>
	  });

	$("input[name=requestType]").change(function() {
		var radioValue = $(this).val();
		var disable = false
		if (radioValue =="D"){
			disable = true;

			$("#protectYn").next().next().addClass("disabled");
			$("#crYn").next().next().addClass("disabled");
		}
		else {
			$("#protectYn").next().next().removeClass("disabled");
			$("#crYn").next().next().removeClass("disabled");
		}

		//$("#userNmPopup").prop("readonly", disable)
		//$("#userPwd").prop("readonly", disable)
		//$("#userPwdConfirm").prop("readonly", disable)
		$("#email").prop("readonly", disable)
		//$("#requestReason").prop("readonly", disable)

	});
});

function selectProtectCountCallback(response){
	if(response.success){

	}else{
		//$("#protectYn").prop('checked', false);
		//$("#protectYn").next().next().removeClass("checked");
		alertMessage(g_msg("msg.duplicateProtect"));
	}
}


function selectCrCountCallback(response){
	if(response.success){

	}else{
		//$("#crYn").prop('checked', false);
		//$("#crYn").next().next().removeClass("checked");
		alertMessage(g_msg("msg.duplicateCr"));
	}
}



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
<div class="dialogContent commonRequestPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible outsideUserInfoRequestPopup">
	<div class="popupHero">
		<h2><spring:message code="title.requestInfo" text="요청 정보" /></h2>
	</div>
	<form id="request" name="request">
	<input id="requestNo" name="requestNo" value="${DetailInfo.requestNo}" type="hidden"/>
	<input id="userCd" name="userCd" value="${DetailInfo.userCd}" type="hidden"/>
	<c:if test="${userCd == ''}">
<%--	<p class="textCaution "><spring:message code="title.pwdNotice" />--%>
	</c:if>
			<ul class="section popupCard popupFormGrid popup-grid-3">
			<li>
				<custom:popupInputText name="companyNm" label="form.companyNm" value="${sessionUser.companyNm }" id="companyNm" readOnly="readOnly"/>
			</li>
		<li>
			<c:if test="${userCd == ''}">
			<custom:popupInputText name="userNm" label="grid.userNm" value="${DetailInfo.userNm}" id="userNmPopup"/>
			</c:if>
			<c:if test="${userCd != ''}">
			<custom:popupInputText name="userNm" label="grid.userNm" value="${DetailInfo.userNm}" id="userNmPopup" readOnly="readOnly"/>
			</c:if>
		</li>
			<c:if test="${userCd == ''}">
			<li>
				<custom:popupInputText name="email" label="form.email" value="${DetailInfo.email}" id="email"/>
			</li>
			<li class="requestTypeField requestTypeFieldGrid">
				<div class="requestTypeInline">
					<label for="requestTypeU">요청 구분</label>
					<div class="clearfix prettyradio labelright">
						<input type="radio" id="requestTypeU" name="requestType" value="I" style="display: none;">
						<a href="#" class="checked" disable></a>
						<label for="requestTypeU">추가</label>
					</div>
					<input id="requestType" name="requestType" value="I" type="hidden"/>
				</div>
			</li>
			</c:if>
			<c:if test="${userCd != ''}">
			<li class="requestTypeField requestTypeFieldGrid">
				<div class="requestTypeInline">
					<label for="">요청 구분</label>
					<custom:popupRadio labelSide="right" options="${userRequestType2}" checkedValue="U" name="requestType"/>
				</div>
			</li>
			</c:if>
		</ul>
		<c:if test="${userCd == ''}">
			<ul class="section popupCard popupFormGrid popup-grid-3">
		<li>
			<custom:popupInputPwd name="userPwd" label="form.pwd" value="${DetailInfo.userPwd}"  id="userPwd" />
		</li>
		<li>
			<custom:popupInputPwd name="userPwdConfirm" label="form.pwdConfirm" value="${DetailInfo.userPwd}" id="userPwdConfirm" />
		</li>
		</ul>
		</c:if>
			<ul class="section popupCard popupFormGrid popup-grid-4 permissionRequestGrid">
				<li class="clearfixCheck permissionCheckField">
					<custom:popupCheckbox options="${informationProtect}" checkedValue="" name="protectYn" labelSide="right" />
				</li>
				<li class="clearfixCheck permissionCheckField">
					<custom:popupCheckbox options="${informationCr}" checkedValue="" name="crYn" labelSide="right" />
				</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-3">
		<li class="full">
			<custom:popupInputTextArea name="requestReason" label="form.requestReason" rows="2" value="${DetailInfo.requestReason}" id="requestReason" disabled="N"/>
		</li>
	</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button id="approvalRequest" class="ui-button ui-corner-all bottomBtn" onclick="requestUser()"><spring:message code="btn.approvalRequest" /></button>
		<button id="close" class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>
