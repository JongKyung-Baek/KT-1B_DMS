<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 페이지 선택 상세보 -->
<script>
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['도면번호','도면명','REV','최종수정일','현재페이지','총페이지'],
			colModel:[
				{name:'col1', index:'col1', width: "100"},
				{name:'col2', index:'col2', width: "100"},
				{name:'col3', index:'col3', width: "100"},
				{name:'col4', index:'col4', width: "80"},
				{name:'col5', index:'col5', width: "50"},
				{name:'col6', index:'col6', width: "90"},
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
			{col1:"XA7000004",col2:"볼트2",col3:"2",col4:"2019-08-28",col5:"1",col6:"1"},
			{col1:"XA7000004",col2:"볼트2",col3:"1",col4:"2019-06-20",col5:"1",col6:"1"},
			{col1:"XA7000004",col2:"볼트2",col3:"A",col4:"2019-05-30",col5:"1",col6:"1"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<style>
	.dialogContent .section{height:100%;}
	.dialogContent .gridContainer{height:100%;}
	.dialogContent .gridContainer .ui-jqgrid{height:100%;}			
	.dialogContent .ui-jqgrid .ui-jqgrid-view{height:100%;}
	.dialogContent .gridContainer .ui-jqgrid .ui-jqgrid-bdiv{width:calc(100% - 38px) !important; max-height:none;}
</style>
<div class="dialogContent">
	<div class="section">
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">선택</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>