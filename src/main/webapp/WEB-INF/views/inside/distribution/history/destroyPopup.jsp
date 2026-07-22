<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="/resources/js/views/inside/distribution/history/destroyPopup.js"></script>
<!-- 배포이력 팝업(폐기 버튼) -->
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
<div class="dialogContent commonRequestPopup destroyPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>폐기 요청</h2>
		<p>폐기 사유를 입력하고 첨부파일을 등록해 주세요.</p>
	</div>
	<form id="formDestoryPopup">
		<ul class="section popupCard popupFormGrid popup-grid-1">
			<li>
				<%-- 상세유형 --%>
				<custom:popupInputText name="destroyType" label="form.destroyType" value="${destroyType }" id="destroyType" disabled="disabled"/>
			</li>
			<li>
				<%-- 폐기사유 --%>
				<custom:popupInputTextArea name="destroyDesc" label="form.destroyDesc" rows="3" value="${destroyDesc }" id="destroyDesc"/>
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
<!-- 	<div class="section"> -->
<!-- 		<div class="dialogToolbar"> -->
<!-- 			<div class="left"> -->
<!-- 				<span class="gridTitle">파일전송</span> -->
<!-- 			</div> -->
<!-- 			<div class="right"> -->
<!-- 				<button class="ui-button ui-corner-all">추가</button> -->
<!-- 				<button class="ui-button ui-corner-all">삭제</button> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 		<div class="fileUploadContainer">fileUploadContainer</div> -->
<!-- 	</div> -->
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<%-- 폐기 등록 --%>
			<custom:popupButton function="saveDestroy()" name="destroy" label="btn.destroyComplete" id="destroy"/>
			<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
		</div>
	</div>
</div>
