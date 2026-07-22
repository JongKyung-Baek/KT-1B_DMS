<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!-- 배포이력 - 도면/문서/SW 팝업(거래중단 버튼)-->
<script>
var popupGridParam;
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});

function setPopupGridParam(){
	popupGridParam = {
			formId : 'formProtectPopup',
			gridId : 'gridProtectPopupList',
			url : '/inside/distribution/commonRequest/selectProtectPopupList',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}

	return popupGridParam;
}

</script>
	<form id="formProtectPopup" name="formProtectPopup">
		<input type="hidden" id="objectId" name="objectId" value='${objectId}'/>
		<input type="hidden" id="objectType" name="objectType" value='${objectType}'/>
	</form>
	
	<div class="dialogContent">
		<div class="section">
			<div class="gridContainer">
				<table id="gridProtectPopupList"></table>
			</div>
		</div>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
		</div>
	</div>
<div id="detailPopupDialog" class="dialogContainer"></div>