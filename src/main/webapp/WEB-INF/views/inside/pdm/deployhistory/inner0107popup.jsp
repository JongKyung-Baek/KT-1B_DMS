<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 출력이력 팝업(출력물폐기요청 버튼)-->
<script>
	$(function() {
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">요청자</label>
			<div><input type="text" id="" value="로그인한 사용자" disabled></div>
		</li>
		<li class="half">
			<label for="">결재자</label>
			<div><select id=""><option selected>김팀장</option></select></div>
			<label for="">방산기술보호책임자</label>
			<div><select id=""><option selected>김방산</option></select></div>
		</li>
	</ul>
	<ul class="section">
		<li>
			<label for="">신청사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">출력승인요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>