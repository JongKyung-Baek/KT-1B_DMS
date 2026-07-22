<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- CR 이력 팝업(제의번호 상세보기)-->
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
			<label for="">변경요청사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
		<li>
			<label for="">검토결과</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
	</ul>
	<ul class="section">
		<li class="half">
			<label for="">제의사유</label>
			<div><input type="text" id="" value="도면오기" disabled></div>
			<label for="">품번</label>
			<div><input type="text" id="" value="A70013323-2" disabled></div>
		</li>
		<li class="half">
			<label for="">도면</label>
			<div><input type="text" id="" value="A70013323" disabled></div>
			<label for="">REV</label>
			<div><input type="text" id="" value="A" disabled></div>
		</li>
		<li class="half">
			<label for="">자재코드</label>
			<div><input type="text" value="PDM00182700" disabled></div>
			<label for="">기종</label>
			<div><select id="" disabled><option value="1120">1120</option></select></div>
		</li>
	</ul>
	<ul class="section">
		<li>
			<label for="">제목</label>
			<div><input type="text" id="" value="도면보기 기술변경 요청 건" disabled></div>
		</li>
		<li>
			<label for="">현상(AS-IS)</label>
			<div><textarea id="" rows="2" disabled>R 5.0</textarea></div>
		</li>
		<li>
			<label for="">설계변경요청(TO-BE)</label>
			<div><textarea id="" rows="2" disabled>2-R 5.0</textarea></div>
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
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>