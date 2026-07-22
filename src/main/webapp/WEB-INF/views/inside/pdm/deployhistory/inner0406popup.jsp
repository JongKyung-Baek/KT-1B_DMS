<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포이력 팝업(접수자 상세보기)-->
<script>
	$(function() {		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['접수자명','부서','직급'],
			colModel:[
				{name:'col1', index:'col1', width: "120"},
				{name:'col2', index:'col2', width: "120"},
				{name:'col3', index:'col3', width: "120"},
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
			{col1:"관리자",col2:"생산기술1팀",col3:"팀장"},
			{col1:"홍길동",col2:"생산기술1팀",col3:"과장"},
			{col1:"홍길동",col2:"생산기술1팀",col3:"과장"},
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
				<span class="sectTitle">접수자 목록</span><span class="listCount">3</span>
			</div>
			<div class="right"></div>
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