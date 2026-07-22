<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- CR요청 팝업(CR신규제의 버튼)-->
<script>
	$(function() {
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['파일명'],
			colModel:[
				{name:'col1', index:'col1', width: "600"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata = [
			{col1:"11-1111-3333"},
			{col1:"22-2222-4444"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">업체담당자</label>
			<div><select id=""><option value="">이업체</option></select></div>
			<label for="">Email</label>
			<div><input type="text"></div>
		</li>
	</ul>
	<ul class="section">
		<li class="half">
			<label for="">제의번호</label>
			<div><input type="text" id="" disabled></div>
			<label for="">제의사유</label>
			<div><select id=""><option>전체</option></select></div>
		</li>
		<li class="half">
			<label for="">도번</label>
			<div><input type="text" id="" class="rightBtn"><button type="button" class="ui-button ui-corner-all rightBtn checkBtn"></button></div>
			<label for="">REV</label>
			<div><input type="text" id=""></div>
		</li>
		<li class="half">
			<label for="">도명</label>
			<div><input type="text" id=""></div>
			<label for="">도면등록일</label>
			<div><input type="text" id="" value="2019-01-01" disabled></div>
		</li>
		<li class="half">
			<label for="">품번</label>
			<div><input type="text" id=""></div>
			<label for="">자재코드</label>
			<div><input type="text" id=""></div>
		</li>
		<li class="half">
			<label for="">사업장</label>
			<div><select id=""><option value="">마곡</option></select></div>
			<label for="">구매담당자</label>
			<div><select id=""><option value="">김구매</option></select></div>
		</li>
	</ul>
	<ul class="section">
		<li>
			<label for="">제목</label>
			<div><input type="text" id=""></div>
		</li>
		<li>
			<label for="">현상(AS-IS)</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
		<li>
			<label for="">설계변경요청(TO-BE)</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
	<p class="textCaution ">CR양식을 빠짐없이 작성하여 첨부하세요. <button type="button" class="ui-button ui-corner-all">CR양식</button></p>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">첨부파일 목록</span><span class="listCount">2</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">파일추가</button>
				<button class="ui-button ui-corner-all">파일삭제</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">CR요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>