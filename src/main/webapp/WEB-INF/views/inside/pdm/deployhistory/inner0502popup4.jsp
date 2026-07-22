<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 업체 및 사용자 관리 팝업(업체사용자 상세보기 및 수정)-->
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
			<label for="">업체명</label>
			<div><select id=""><option value="한화디펜스">한화디펜스</option></select></div>
		</li>
		<li>
			<label for="">사용자명</label>
			<div><input type="text" id="" value="홍길동"></div>
		</li>
		<li>
			<label for="">이메일</label>
			<div><input type="text" id="" value="esob@esob.kr"></div>
		</li>
		<li>
			<label for="">암호</label>
			<div><input type="text" id="" value="1246789"></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">수정</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>