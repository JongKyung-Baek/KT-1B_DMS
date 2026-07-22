<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 현황 및 폐기 팝업(전체폐기 버튼)-->
<script>
	$(function() {
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="">요청자</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li>
			<label for="">결재자</label>
			<div><select id=""><option value=""></option></select></div>
		</li>
		<li>
			<label for="">폐기사유</label>
			<div><textarea id="" rows="3"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">승인요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>