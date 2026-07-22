<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 검색 및 배포요청 팝업(일괄검색 버튼)-->
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#partialMatch").prettyCheckable({labelPosition:'right'});
	});
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['자료번호'],
			colModel:[
				{name:'col1', index:'col1', width: "300"},
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
			{col1:"20161233-D0014"},
			{col1:"20161233-D0014"},
			{col1:"20161233-D0014"},
			{col1:"20161233-D0014"},
			{col1:"20161233-D0014"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<input type="checkbox" id="partialMatch" name="checkMatch" checked="checked"><label for="partialMatch">부분일치</label>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">붙여넣기</button>
				<button class="ui-button ui-corner-all">검색</button>
				<button class="ui-button ui-corner-all">삭제</button>
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
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>