<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 사용자 정보 팝업(사용자 정보 변경 버튼) -->
<script>
	$(document).ready(function(){
	});
	$(function() {	
	});
</script>
<style>
	
</style>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">업체명</label>
			<div class="textOnly">와우소프트</div>
		</li>
		<li class="half">
			<label for="">성명</label>
			<div class="textOnly">홍길동</div>
			<label for="">Email</label>
			<div><input type="text" id=""></div>
		</li>
		<li class="half">
			<label for="">현재 비밀번호</label>
			<div><input type="text" id=""></div>
			<div class="textOnly red">9자 이상, 영문/숫자/특수문자 포함</div>
		</li>
		<li class="half">
			<label for="">변경 비밀번호</label>
			<div><input type="text" id=""></div>
			<label for="">변경 비밀번호 확인</label>
			<div><input type="text" id=""></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">저장</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>