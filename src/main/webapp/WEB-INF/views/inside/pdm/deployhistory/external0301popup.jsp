<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포요청 팝업(배포요청 버튼) -->
<script>
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['자료번호','자료명','REV','S/W버전','명세서버전','세부유형','자료등록일','방산기술'],
			colModel:[
				{name:'col1', index:'col1', width: "100"},
				{name:'col2', index:'col2', width: "100"},
				{name:'col3', index:'col3', width: "50"},
				{name:'col4', index:'col4', width: "90"},
				{name:'col5', index:'col5', width: "90"},
				{name:'col6', index:'col6', width: "90"},
				{name:'col7', index:'col7', width: "90"},
				{name:'col8', index:'col8', width: "100"},
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
			{col1:"BIHO_002",col2:"사격펄스 측정시험장비",col3:"",col4:"",col5:"",col6:"소스파일",col7:"2019-02-21",col8:""},
			{col1:"BIHO_002",col2:"사격펄스 측정시험장비",col3:"",col4:"",col5:"",col6:"소스파일",col7:"2015-02-21",col8:""}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<p class="textCaution">기 배포된 도면을 재 요청할 경우, 기존 도면은 신규도면 배포와 동시에 자동으로 폐기됩니다.</p>
	<ul class="section">
		<li class="half">
			<label for="input1">요청번호</label>
			<div><input type="text" id="input1" disabled="disabled" value="자동채번"></div>
			<label for="input2">용도</label>
			<div><select id="input2"><option>선택</option></select></div>
		</li>
		<li class="half">
			<label for="input3">업체명/담당자</label>
			<div><select id="input3"><option>전체</option></select></div>
			<label for="">Email</label>
			<div><input type="text" id="input4" placeholder="로그인 사용자 Email"></div>
		</li>
		<li class="half">
			<label for="input5">구매담당자</label>
			<div><select id="input5"><option>전체</option></select></div>
		</li>
		<li>
			<label for="input6">요청사유</label>
			<div><textarea id="input6" rows="3"></textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">문서목록</span><span class="listCount">2</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all ">점검</button>
				<button class="ui-button ui-corner-all ">붙여넣기</button>
				<button class="ui-button ui-corner-all ">추가</button>
				<button class="ui-button ui-corner-all ">삭제</button>
				<button class="ui-button ui-corner-all ">전체삭제</button>
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
		<button class="ui-button ui-corner-all bottomBtn">배포요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>