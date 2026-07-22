<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포승인 팝업(요청번호 상세보기)-->
<script>
	$(function() {
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업장','부서','성명','배포','폐기(예정)'],
			colModel:[
				{name:'col1', index:'col1', width: "180"},
				{name:'col2', index:'col2', width: "130"},
				{name:'col3', index:'col3', width: "120"},
				{name:'col4', index:'col4', width: "100"},
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
			{col1:"창원 1사업장",col2:"용접직",col3:"김길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"용접직",col3:"이길동",col4:"30",col5:""},
			{col1:"창원 1사업장",col2:"용접직",col3:"박길동",col4:"30",col5:""},
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
				{name:'col1', index:'col1', width: "120"},
				{name:'col2', index:'col2', width: "150"},
				{name:'col3', index:'col3', width: "150"},
				{name:'col4', index:'col4', width: "100"},
				{name:'col5', index:'col5', width: "70"},
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
		<li>
			<label for="">반려사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">접수자 정보 목록</span><span class="listCount">5</span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포승인 자료 목록</span><span class="listCount">2</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">Viewing</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD2></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">배포승인</button>
		<button class="ui-button ui-corner-all bottomBtn">배포반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>