<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 사용자 변경  팝업(사용자 변경 버튼) -->
<script>
	$(document).ready(function(){
	});
	$(function() {	
	});
</script>
<style>
	
</style>
<div class="dialogContent">
	<p class="textCaution">변경할 접속 정보를 입력하세요</p>
	<form id="loginForm" name="loginForm" action="/login/loginProcess" method="POST">
		<ul class="section">
			<li>
				<label for="userId">ID</label>
				<div><input type="text" id="userId" name="userId" placeholder="ID을 입력하세요" autofocus></div>
			</li>
			<li>
				<label for="userPw">Password</label>
				<div><input type="password" id="userPw"  name="userPw" placeholder="Password를 입력하세요"></div>
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">사용자 변경</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>