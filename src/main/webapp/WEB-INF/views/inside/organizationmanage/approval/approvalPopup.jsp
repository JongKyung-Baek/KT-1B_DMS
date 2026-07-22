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
};



	function approval2(){
		if("" != $('#rejectReason').val()) {
			alertMessage(g_msg("msg.rejectReasonApproal")); //반려사유가 존재하면 승인할 수 없습니다.
			return;
		}
		var param = $('#approval').serializeObject();
		callAjax(param, "/inside/organizationmanage/approval/approval", approvalCallback);
	};
	/**
	 * 배포요청 후 결과 메시지 출력
	 * @param response
	 * @returns
	 */
	function approvalCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	};

	function reject(){
		if(!isValidObjEmpty($("#rejectReason"), 'form.rejectDesc')) return false;
		var param = $('#approval').serializeObject();
		callAjax(param, "/inside/organizationmanage/approval/reject", rejectCallback);
	};
	/**
	 * 배포요청 후 결과 메시지 출력
	 * @param response
	 * @returns
	 */
	function rejectCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.completeReject'), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	};
</script>
	<div class="dialogContent">
		<form id="approval" name="approval">
		<input id="targetUserCd" name="targetUserCd" value="${DetailInfo.targetUserCd}" type="hidden" />
		<input id="userPwd" name="userPwd" value="${DetailInfo.userPwd}" type="hidden" />
		<input id="companyCd" name="companyCd" value="${DetailInfo.companyCd}" type="hidden" />
		<input id="requestType" name="requestType" value="${DetailInfo.requestType}" type="hidden" />
		<input id="protectYn" name="protectYn" value="${DetailInfo.protectYn}" type="hidden" />
		<input id="crYn" name="crYn" value="${DetailInfo.crYn}" type="hidden" />

		<ul class="section">
			<li class="half">
				<custom:popupInputText name="requestNo" value="${DetailInfo.requestNo }" label="form.requestNo" id="requestNo" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="userNm" value="${DetailInfo.userNm }" label="form.companyUser" id="companyUser" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="companyNm" value="${DetailInfo.companyNm }" label="form.companyNm" id="companyNm" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="email" value="${DetailInfo.email }" label="form.email" id="email" readOnly="readOnly"/>
			</li>

			<li class="half">
				<custom:popupCheckbox options="${informationProtect}" checkedValue="${DetailInfo.protectYn }" name="protectYnShow" labelSide="left" disabled="disabled" />
			</li>
			<li class="half">
				<custom:popupCheckbox options="${informationCr}" checkedValue="${DetailInfo.crYn }" name="crYnShow" labelSide="left" disabled="disabled" />
			</li>
			<li class="half">
				<custom:popupInputTextArea name="requestReason" value="${DetailInfo.requestReason }" label="form.requestReason" rows="4" id="requestReason" disabled="Y"/>
			</li>

		</ul>

		<ul class="section">
			<li class="half">
				<custom:popupSelectBox label="form.userRequestType" options="${userRequestType}" selectedValue="${DetailInfo.requestType }" id="requestTypeShow" name="requestTypeShow" disabled="Y"/>
			</li>
			<li class="half">
				<c:if test="${statusCd eq 'REQUEST'}">
					<custom:popupInputTextArea name="rejectReason" value="${rejectReason }" label="form.rejectDesc" rows="4" id="rejectReason"/>
				</c:if>
				<c:if test="${statusCd ne 'REQUEST'}">
					<custom:popupInputTextArea name="rejectReason" value="${rejectReason }" label="form.rejectDesc" rows="4" id="rejectReason" disabled="Y"/>
				</c:if>
			</li>
		</ul>

		</form>
	</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<c:if test="${statusCd eq 'REQUEST'}">
			<button class="ui-button ui-corner-all bottomBtn" onclick="approval2()"><spring:message code="btn.userApproval" /></button>
			<button class="ui-button ui-corner-all bottomBtn" onclick="reject()"><spring:message code="btn.reject" /></button>
		</c:if>
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>