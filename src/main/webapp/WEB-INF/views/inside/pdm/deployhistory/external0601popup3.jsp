<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 사용자 정보 팝업(요청번호 (추가)상세보기) -->
<script>
	$(document).ready(function(){
		$("#input11").prettyCheckable({labelPosition:'left'});
		$("#input12").prettyCheckable({labelPosition:'right'});
		$("#input13").prettyCheckable({labelPosition:'right'});
		$("#input14").prettyCheckable({labelPosition:'right'});
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
			<div><input type="text" id="" disabled></div>
			<label for="">Email</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li class="half">
			<label for="">비밀번호</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li>
			<label for="input11">방산기술보호 권한 요청</label>
			<input type="checkbox" name="checkbox" id="input11" checked disabled>
		</li>
		<li>
			<label for="">요청 구분</label>
			<div>
				<label for="input12">신규</label><input type="radio" name="inputcheck" id="input12" checked disabled>
				<label for="input13">변경</label><input type="radio" name="inputcheck" id="input13" disabled>
				<label for="input14">삭제</label><input type="radio" name="inputcheck" id="input14" disabled>
			</div>
		</li>
		<li>
			<label for="">요청사유</label>
			<div><textarea id="" rows="3" disabled></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>