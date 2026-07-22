<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- QnA 팝업(추가 버튼)-->
<script>
	$(function() {	
	});
</script>
<style>
	textarea{height:200px;}
</style>
<div class="dialogContent noticeVeiw">
	<ul class="section">
		<li class="half">
			<label for="">등록자</label>
			<div class="textOnly">이왕근</div>
			<label for="">등록일</label>
			<div class="textOnly">201-01-01</div>
		</li>
	</ul>
	<ul class="section">
		<li>
			<label for="">제목</label>
			<div>
				<input type="text" id="">
			</div>
		</li>
		<li>
			<label for="">내용</label>
			<div>
				<textarea id="" rows="3"></textarea>
			</div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">등록</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>