<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<!-- 사용자 변경  팝업(사용자 변경 버튼) -->
<script>
	$(document).ready(function(){
	});
	$(function() {
	});

	function isValidation(){
		//비밀번호
		if($.trim($("#userPwd").val()) === ""){
			isValidDataEmpty("userPwd", "form.pwd");
			return false;
		}
		if($.trim($("#userNewPwd").val()) === ""){
			isValidDataEmpty("userNewPwd", "form.newPwd");
			return false;
		}
		// if(checkPassword($.trim($("#userNewPwd").val())) == true){
		// 	alertMessage(g_msg('msg.pwdNotice'));
		// 	return false;
		// }
		if($.trim($("#userNewPwd2").val()) === ""){
			isValidDataEmpty("userNewPwd2", "form.newPwdConfirm");
			return false;
		}
		if($.trim($("#userNewPwd").val()) !== $.trim($("#userNewPwd2").val())){
			alertMessage(g_msg('msg.pwdCompare'));
			return false;
		}
		
		//email
		if($.trim($("#email").val()) === ""){
			isValidDataEmpty("email", "form.email");
			return false;
		}
		
		return true;
	}

	function checkPassword(password){
	    if(!/^(?=.*[a-zA-Z])(?=.*[~`!@#$%^&*+=-])(?=.*[0-9]).{9,25}$/.test(password)){
	        return true;
	    }
	    var checkNumber = password.search(/[0-9]/g);
	    var checkEnglish = password.search(/[a-z]/ig);
	    if(checkNumber <0 || checkEnglish <0){
	        return true;
	    }
	    return false;
	}
	
	function updateUser(){
		if(!isValidation()){
			return;
		}

		var param = $('#updateForm').serializeObject();
		callAjax(param, "/outside/user/information/update", updateUserCallback);
	}

	/**
	 * 사용자변경 후 결과 메시지 출력
	 * @param response
	 * @returns
	 */
	function updateUserCallback(response){
		if(response.success){
			infoMessage(g_msg('msg.completeSave'), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.failSave"));
		}
	}
</script>
<style>

</style>
<div class="dialogContent commonRequestPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible outsideUserModPopup">
	<div class="popupHero">
		<h2><spring:message code="title.userInfo" text="사용자 정보 변경" /></h2>
	</div>
<%--	<p class="textCaution "><spring:message code="title.pwdNotice" />--%>
	<form id="updateForm" name="updateForm">
		<input type="hidden" id="userCd" name="userCd" value="${sessionUser.userCd }"/>

		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li>
				<custom:popupInputText name="companyNm" label="form.companyNm" value="${sessionUser.companyNm}" id="companyNm" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupInputText name="email" label="form.email" value="${sessionUser.email}" id="email" />
			</li>
			<li>
				<custom:popupInputText name="userNm" label="grid.userNm" value="${sessionUser.userNm}" id="userNm" readOnly="readOnly" />
			</li>
			<li class="empty"></li>
		</ul>

		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li>
				<label for="userPwd"><spring:message code="form.pwd" text="현재 비밀번호" /></label>
				<div><input type="password" id="userPwd" name="userPwd" placeholder="Password를 입력하세요"></div>
			</li>
			<li>
				<label for="userNewPwd"><spring:message code="form.newPwd" text="변경 비밀번호" /></label>
				<div><input type="password" id="userNewPwd" name="userNewPwd" placeholder="Password를 입력하세요"></div>
			</li>
			<li>
				<label for="userNewPwd2"><spring:message code="form.newPwdConfirm" text="변경 비밀번호 확인" /></label>
				<div><input type="password" id="userNewPwd2" name="userNewPwd2" placeholder="Password를 입력하세요"></div>
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button id="save" class="ui-button ui-corner-all bottomBtn" onclick="updateUser()">사용자 변경</button>
		<button id="close" class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
