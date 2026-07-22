<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="/resources/js/views/outside/commondestroy/commonDestroyRequestPopup.js"></script>
<script>
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
// 	$("#gridDestroyUploadPopup .cbox").prettyCheckable();
	$("#"+popupGridId).jqGrid('clearGridData');
});

function setGridParam(){
	popupGridParam = {
			gridId : popupGridId,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check',
			cellEdit: true,
			cellsubmit: 'clientArray'
	};
}
</script>
<div class="dialogContent commonDestroyRequestPopup popup-base popup-actions-center popup-type-upload-grid">
	<div class="popupHero">
		<h2>폐기 요청</h2>
		<p>폐기 사유와 승인 담당자를 지정하고 첨부 파일을 등록해 주세요.</p>
	</div>
	<form id="formDestoryPopup">
		<input type="hidden" name="requestType" value="${requestType }"/>
		<input type="hidden" name="destroyType" value="${destroyType}"/>
		<ul class="section popupCard popupFormGrid popup-grid-1">
			<li>
				<%-- 상세현황 --%>
				<custom:popupInputText name="destroyTypeNm" label="form.destroyType" value="${destroyTypeNm }" id="destroyTypeNm" disabled="disabled"/>
			</li>
			<li>
				<%-- 폐기사유 --%>
				<custom:popupInputTextArea name="destroyDesc" label="form.destroyDesc" rows="3" value="${destroyDesc }" id="destroyDesc"/>
			</li>
			<li>
				<!-- 구매담당자 -->
				<custom:popupSelectBox name="approvalUser" options="${approvalUser }" selectedValue="${approvalUser[0].selectedValue}" label="form.approvalUser" id="approvalUser" />
			</li>
		</ul>
	</form>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code="form.uploadFileList" /></span><span class="listCount" id="listCount"></span>
			</div>
			<div class="right">
				<form id="fileForm" name="fileForm" enctype="multipart/form-data">
					<input type="file" id="destroyFile" name="destroyFile" multiple="multiple" onchange="fileAppendEvent()" style="display: none;"/>
				</form>
				<button class="ui-button ui-corner-all" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
				<button class="ui-button ui-corner-all" onclick="fileDelete()"><spring:message code="btn.fileDelete"/></button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDestroyUploadPopup"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 폐기 등록 --%>
		<custom:popupButton function="saveDestroy()" name="destroy" label="btn.destroyComplete" id="destroy"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
