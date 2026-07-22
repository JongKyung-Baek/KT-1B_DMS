<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/disposalacceptance/approvalPopup.js"></script>
<!-- 폐기 승인 -->
<script>
var popupGridParam;
var gridId='gridDisPosalApprovalPopup';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam());
});

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'DISTRIBUTION\', \'"+rowdata["objectType"]+"\', \'" +rowdata["requestNo"] +"\', \'" + rowdata["objectId"] +"\', \'" + rowdata["fileNo"] + "\', \'" + rowdata["protectYn"] + "\')">'+cellValue+'</a>';
}

function setPopupGridParam(){
	popupGridParam = {
			gridId : gridId,
			formId : 'formAcceptancePopup',
			url : '/inside/distribution/disposalacceptance/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}

	return popupGridParam;
}

function formatAddMonth(cellValue, options, rowdata, action){
	if(undefined == cellValue){
		return '';
	}else{
		return cellValue + g_msg("msg.month");
	}
}

function fileDownload(destroyNo, destroyFileSeq){
	$("form[name=tmpForm]")
	.attr("action","/inside/distribution/disposalacceptance/fileDownload?destroyNo="+destroyNo+"&destroyFileSeq="+destroyFileSeq)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
}

function saveApproval(flag){
	if('A' == flag){
		confirmMessage(g_msg("msg.approvalDestory"), function(){
			$(this).dialog("close");

			let param = $('#formAcceptancePopup').serializeObject();
			param.saveFlag = flag;

			callAjax(param, "/inside/distribution/disposalacceptance/saveApproval", function(response){
				alertMessage(g_msg('msg.completeApprove'), function(){
					searchList(gridParam);
					closePopup('popupDialog');
					$(this).dialog('close');
				})
			}, 'json', false);
		});
	}

	if('R' == flag){
		if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectDesc')) {
			return false;
		}

		confirmMessage(g_msg("msg.rejectDestory"), function(){
			$(this).dialog("close");

			let param = $('#formAcceptancePopup').serializeObject();
			param.saveFlag = flag;
			param.rejectDesc = $('#rejectDesc').val();

			callAjax(param, "/inside/distribution/disposalacceptance/saveApproval", function(response){
				alertMessage(g_msg('msg.completeReject'), function(){
					searchList(gridParam);
					closePopup('popupDialog');
					$(this).dialog('close');
				})
			}, 'json', false);
		});
	}
}
</script>
<div class="dialogContent commonRequestPopup disposalAcceptancePopup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero">
		<h2>폐기 승인</h2>
		<p>요청 정보를 확인하고 승인 또는 반려를 진행해 주세요.</p>
	</div>
	<form id="formAcceptancePopup">
		<input id="destroyRequestNo" name="destroyRequestNo" value="${formData.destroyRequestNo}" type="hidden"/>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<custom:popupInputTextArea name="requestDesc" label="form.requestDesc" rows="2" value="${formData.requestDesc}" id="requestDesc" disabled="Y"/>
			</li>
			<li class="half">
				<custom:popupInputTextArea name="rejectDesc" label="form.rejectDesc" rows="2" value="${formData.rejectDesc}" id="rejectDesc"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half full-row">
				<label>첨부파일</label>
				<div class="fileDownload">
					<c:forEach items="${fileList}" var="fileInfo">
						<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload(${fileInfo.destroyNo}, ${fileInfo.destroyFileSeq })'>
							<span>${fileInfo.fileName }</span>
						</button>
					</c:forEach>
				</div>
			</li>
			<li class="half">
				<custom:popupInputText name="requestCompanyNm" id="requestCompanyNm" label="form.companyNm" value="${formData.requestCompanyNm }" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="requestUserNm" id="requestUserNm" label="form.vendorUser" value="${formData.requestUserNm }" readOnly="readOnly"/>
			</li>
			<li class="half">
				<custom:popupInputText name="requestDt" id="requestDt" label="form.requestDt" value="${formData.requestDt }" readOnly="readOnly"/>
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">배포요청 접수목록</span><span class="listCount" id="listCount"></span>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDisPosalApprovalPopup"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<c:if test="${formData.statusCd == 'REQUEST' && formData.purchaserUserCd == sessionUser.userCd}">
			<custom:popupButton function="saveApproval('A')" name="approval" label="btn.destroyApproval" id="approval"/>
			<custom:popupButton function="saveApproval('R')" name="reject" label="btn.destroyReject" id="reject"/>
		</c:if>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
<form name="tmpForm"></form>
