<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포이력 팝업(폐기 버튼)-->
<script>
	$(function() {
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="input1">상세현황</label>
			<div><input type="text" id="input1" disabled="disabled" value="자동폐기"></div>
		</li>
		<li>
			<label for="input2">폐기사유</label>
			<div><textarea id="input2" rows="3">업체거래중단으로 폐기처리함.(자동 폐기일 경우 자동폐기라고 명시됨)</textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">파일전송</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">추가</button>
				<button class="ui-button ui-corner-all">삭제</button>
			</div>
		</div>
		<div class="fileUploadContainer">fileUploadContainer</div>
	</div>
</div>	
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">폐기 등록</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>