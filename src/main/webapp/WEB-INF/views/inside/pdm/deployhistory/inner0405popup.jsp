<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 폐기 승인 팝업(폐기승인/반려 버튼)-->
<script>
	$(function() {
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="">요청자</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li>
			<label for="">폐기사유</label>
			<div><textarea id="" rows="3" disabled>단종(종산)으로 전체 폐기함</textarea></div>
		</li>
		<li>
			<label for="">반려사유</label>
			<div><textarea id="" rows="3"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">폐기승인</button>
		<button class="ui-button ui-corner-all bottomBtn">폐기반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>