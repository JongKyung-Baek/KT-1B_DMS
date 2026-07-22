<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/outside/commonrequest/commonRequestDetailPopup.js"></script>
<script type="text/javascript" src="/resources/js/views/inside/common/clipboard.js"></script>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script>
var popupInfo = ${popupInfo};
var popupGridParam2;
var detailPopupGridId = 'gridRequestItemDetailList';
var itemInfo = ${itemInfo};
$(document).ready(function(){
	setGridParam();
	settingGrid('${gridInfo }', popupGridParam2, 'popupGridParam2');
	$("#"+detailPopupGridId).jqGrid('clearGridData');
});


function setGridParam(){

	popupGridParam2 = {
			gridId : detailPopupGridId,
			postData: itemInfo,
			url : popupInfo.urlInfo + 'selectItemDetailList',
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check',
	};
}


</script>
	<!-- 배포요청 상세 선택 팝업 -->
		<div class="dialogContent commonRequestDetailPopup popup-base popup-actions-center popup-type-form-grid">
			<div class="popupHero">
				<h2>상세 항목 선택</h2>
				<p>요청 대상의 세부 항목을 선택한 뒤 저장할 수 있습니다.</p>
			</div>
			<div class="section popupCard">
				<div class="gridContainer">
					<table id="gridRequestItemDetailList"></table>
				</div>
			</div>
		</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<button class="ui-button ui-corner-all bottomBtn" onclick="save()"><spring:message code="btn.save" /></button>
			<button class="ui-button ui-corner-all bottomBtn" onclick="$('#detailPopupDialoga').dialog('close')"><spring:message code="btn.close" /></button>
		</div>
	</div>
