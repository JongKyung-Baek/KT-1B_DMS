<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script type="text/javascript" src="/resources/js/views/inside/common/searchAllPopup.js"></script>
<!-- SW 팝업(일괄검색 버튼)-->
<script>
var popupInfo = ${popupInfo};
var popupGridParam;
var searchAllGridId = 'gridSearchAll';

$(document).ready(function(){
	setPopupGridParam();
	settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
	$("#"+searchAllGridId).jqGrid('clearGridData');
	$("#partialMatch").prettyCheckable({labelPosition:'right'});
	gridParam.useLike = 'N';
});

function setPopupGridParam(){
	popupGridParam = {
			gridId : searchAllGridId,
			multiSelect : true,
			numbering : false,
			shrinkToFit: true,
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			selectRowAction : 'check',
	};
}
</script>
<div class="dialogContent searchAllPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>일괄 검색</h2>
	</div>
	<div class="section popupCard">
		<div class="dialogToolbar">
			<div class="left">
				<input type="checkbox" id="partialMatch" name="partialMatch"><label for="partialMatch">부분일치</label>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all" onclick="paste()">붙여넣기</button>
				<button class="ui-button ui-corner-all" onclick="searchList(gridParam)">검색</button>
				<button class="ui-button ui-corner-all" onclick="deleteRow()">삭제</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSearchAll"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" onclick="$('#searchAllPopupa').dialog('close')">닫기</button>
	</div>
</div>
