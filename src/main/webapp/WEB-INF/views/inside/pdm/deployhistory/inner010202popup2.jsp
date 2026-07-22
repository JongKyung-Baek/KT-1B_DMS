<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 문서 팝업 (출력승인요청 버튼) -->
<script>
	$(function() {
	});
</script>
<div class="dialogContent">
	<p class="textCaution">출력기간 : 팀장 승인 후 1주일 / 출력 횟수 : 3회</p>
	<ul class="section">
		<li class="half">
			<label for="">요청자</label>
			<div><select id="" disabled><option selected>김요청</option></select></div>
		</li>
		<li class="half">
			<label for="">결재자</label>
			<div><select id=""><option selected>김팀장</option></select></div>
			<label for="">방산기술보호책임자</label>
			<div><select id=""><option selected>김방산</option></select></div>
		</li>
	</ul>
	<ul class="section">
		<li class="half">
			<label for="">용도</label>
			<div><select id=""><option selected>참고용</option></select></div>
			<label for="">폐기기한</label>
			<div><input type="text" value="2019-03-01" disabled></div>
		</li>
		<li>
			<label for="">신청사유</label>
			<div><textarea id="" rows="3"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">배포승인요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>