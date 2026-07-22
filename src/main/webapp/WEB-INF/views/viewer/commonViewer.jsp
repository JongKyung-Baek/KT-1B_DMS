<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!-- Viewing을 위한 파일 목록 리스트 -->
<script>
var filePopupGridParam;
var gridId='gridCommonViewingPopup';
var VIEWER_URL = '${viewerUrl}';
$(function() {
	filePopupGridParam = setPopupGridParam();
	settingGrid('${gridInfo }', filePopupGridParam, 'filePopupGridParam');
});

function setPopupGridParam(){
	filePopupGridParam = {
			gridId : gridId,
			formId : 'formCommonViewerPopup',
			url : '/common/viewer/selectList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return filePopupGridParam;
}

function formatFileOrgNm(cellValue, options, rowdata, action) {
	//function openFile(requestType, objectType, requestNo, objectId, fileNo, protectYn)
	//return '<a onclick="javascript:alert('a')">'+cellValue+'</a>';
	return '<a onclick="openFile(\'' + rowdata["requestType"] + '\', \''+rowdata["objectType"]+'\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
}



function gridComplete(){
	//리스트 갯수
	var $grid = $('#gridCommonViewingPopup');
	$('#fileCount').html($grid.getGridParam('records'));
}
</script>
<style>
	#formCommonViewerPopup + .dialogContent .section{height:100%; margin-top:0;}
	#formCommonViewerPopup + .dialogContent .gridContainer{height:calc(100% - 35px);}
	#formCommonViewerPopup + .dialogContent .gridContainer .ui-jqgrid{height:100%;}
	#formCommonViewerPopup + .dialogContent .ui-jqgrid .ui-jqgrid-view{height:100%;}
	#formCommonViewerPopup + .dialogContent .gridContainer .ui-jqgrid .ui-jqgrid-bdiv{height:calc(100% - 38px) !important; max-height:none;}
</style>
	<form id="formCommonViewerPopup">
		<input id="objectId" name="objectId" value="${objectId}" type="hidden"/>
		<input id="requestNo" name="requestNo" value="${requestNo}" type="hidden"/>
		<input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
	</form>
<div class="dialogContent">
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<%-- 파일 목록 --%>
				<span class="gridTitle"><spring:message code="title.fileList"/></span><span class="listCount" id="fileCount"></span>
			</div>
				<div class="right">
				<c:if test="${'APPROVAL' == actionCd }">
					<button class="ui-button ui-corner-all" onclick="openPrintViewer('DISTRIBUTION', 'gridCommonViewingPopup', 'IN')"><spring:message code="label.print"/></button>
				</c:if>
				</div>
		</div>
		<div class="gridContainer">
			<table id="gridCommonViewingPopup"></table>
		</div>
	</div>

</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="closePopup('viewerDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
