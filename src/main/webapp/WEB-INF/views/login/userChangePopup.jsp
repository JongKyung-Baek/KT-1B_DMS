<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>	
<!-- 사용자 변경 버튼(관리자 전용) -->
<script>
var popupGridParam;
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
});
function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridUserChangePopup',
			formId : 'loginForm',
			url : '/login/selectList',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}
</script>
<div class="dialogContent">
	<div class="sbr">
			<form id="loginForm" name="loginForm" action="/login/loginProcess" method="POST">
			<ul class="ibx">
				<li>
					<%-- 업체명 --%>
					<label><spring:message code="form.userNmChangePopup"/></label>
					<input id="userNm" name="userNm">
				</li>
			</ul>
		</form>
		<div class="btnBox">
			<%--조회 --%>
			<custom:popupButton function="searchList(popupGridParam)" name="searchPopup" label="btn.search" id="searchPopup"/>
		</div>
	</div>
	<div class="section">
		<div class="gridContainer">
			<table id="gridUserChangePopup"></table>
			<div id="gridUserChangePopupPager"></div>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" type="submit">로그인</button>
	</div>
</div>
</form>