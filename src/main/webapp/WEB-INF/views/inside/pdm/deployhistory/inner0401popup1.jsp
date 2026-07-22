<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 검색 및 배포요청 팝업(생산기술자료 배포 승인요청 버튼)-->
<script>
	$(function() {
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#radio1_1").prettyCheckable({labelPosition:'left'});
		$("#radio1_2").prettyCheckable({labelPosition:'left'});
		$("#radio1_3").prettyCheckable({labelPosition:'left'});
		$("#radio2_1").prettyCheckable({labelPosition:'left'});
		$("#radio2_2").prettyCheckable({labelPosition:'left'});
		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업장','배포부서','성명','배포','폐기(예정)'],
			colModel:[
				{name:'col1', index:'col1', width: "200"},
				{name:'col2', index:'col2', width: "150"},
				{name:'col3', index:'col3', width: "150"},
				{name:'col4', index:'col4', width: "120"},
				{name:'col5', index:'col5', width: "120"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata = [
			{col1:"창원 1사업장",col2:"제작직",col3:"김길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"제작직",col3:"이길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"제작직",col3:"박길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"제작직",col3:"최길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"제작직",col3:"한길동",col4:"30",col5:""},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
		
		$("#jqGridD2").jqGrid({
			datatype: "local",
			colNames:['기종','자료유형','자료번호','문서명','REV'],
			colModel:[
				{name:'col1', index:'col1', width: "150"},
				{name:'col2', index:'col2', width: "200"},
				{name:'col3', index:'col3', width: "200"},
				{name:'col4', index:'col4', width: "150"},
				{name:'col5', index:'col5', width: "100"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata = [
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-1",col4:"Cargo-1",col5:"0"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-2",col4:"Cargo-1",col5:"0"}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD2").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">요청자</label>
			<div><input type="text" id=""></div>
			<label for="">팀장</label>
			<div><select id=""><option value=""></option></select></div>
		</li>
		<li class="half">
			<label for="">유효기간</label>
			<div><select id=""><option value=""></option></select></div>
			<label for="">유효기간 워터마크</label>
			<div>
				<label for="radio2_1">출력</label><input type="radio" name="radio2" id="radio2_1">
				<label for="radio2_2">미출력</label><input type="radio" name="radio2" id="radio2_2">
			</div>
		</li>
		<li>
			<label for="">배포유형</label>
			<div>
				<label for="radio1_1">신규</label><input type="radio" name="radio1" id="radio1_1">
				<label for="radio1_2">기존대체</label><input type="radio" name="radio1" id="radio1_2">
				<label for="radio1_3">추가</label><input type="radio" name="radio1" id="radio1_3">
			</div>
		</li>
		<li class="half">
			<label for="">배포처</label>
			<div><select id=""><option value=""></option></select></div>
			<label for="">배포일자</label>
			<div><select id=""><option value=""></option></select></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포처 목록</span><span class="listCount">5</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">Line 추가</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포 자료 목록</span><span class="listCount">2</span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD2></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">생산기술자료 배포승인 요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>