<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 변경요청 접수 팝업(요청번호 상세보기)-->
<script>
	$(function() {	
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#input12").prettyCheckable({labelPosition:'left'});
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">요청번호</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li>
			<label for="">요청사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
		<li class="half">
			<label for="">사용자성명</label>
			<div><input type="text" disabled></div>
			<label for="">이메일</label>
			<div><input type="text" disabled></div> 
		</li>
		<li class="half">
			<label for="">비밀번호</label>
			<div><input type="text" disabled></div>
		</li>
		<li class="half"> 
			<label for="">협력업체</label>
			<div><input type="text" disabled></div>
			<label for="input12">방산기술 보호대상 요청</label>
			<input type="checkbox" name="input" id="input12" checked disabled>
		</li>
	</ul>
	<ul class="section">
		<li class="half">
			<label for="">협력업체</label>
			<div><select id=""><option value="추가">추가</option></select></div>
		</li>
		<li>
			<label for="">거부사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">승인</button>
		<button class="ui-button ui-corner-all bottomBtn">거부</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>