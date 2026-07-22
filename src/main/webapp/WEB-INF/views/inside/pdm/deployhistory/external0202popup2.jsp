<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포승인현황 팝업(첨부파일 상세보기) -->
<script>
	$(document).ready(function(){
	});
</script>
<style>
	.dialogContent ul.section li div .fileDownload{}
	.dialogContent ul.section li .fileDownload + .fileDownload{margin-top:10px;}
</style>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="input1">상세현황</label>
			<div><input type="text" id="input1" disabled="disabled" value="자동폐기"></div>
		</li>
		<li>
			<label for="input2">폐기사유</label>
			<div><textarea id="input2" rows="3"></textarea></div>
		</li>
		<li>
			<label for="">첨부파일<span class="listCount">3</span></label>
			<div>
				<div class="fileDownload"><button type="button" class="ui-button ui-corner-all fileDownBtn"><span>설계변경.xls</span></button></div>
				<div class="fileDownload"><button type="button" class="ui-button ui-corner-all fileDownBtn"><span>aaa.ppt</span></button></div>
				<div class="fileDownload"><button type="button" class="ui-button ui-corner-all fileDownBtn"><span>bbb.ppt</span></button></div>
			</div>	
		</li>
	</ul>
</div>	
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">배포요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>

