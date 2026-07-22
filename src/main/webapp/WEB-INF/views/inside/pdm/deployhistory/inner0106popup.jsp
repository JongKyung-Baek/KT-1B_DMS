<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 출력승인 팝업(출력요청번호 상세보기)-->
<script>
	$(function() {		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업구분','자료유형','자료번호','REV','S/W버전','명세서버전','자료명','페이지','총페이지','기종','사업장','방산기술'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2', width: "80"},
				{name:'col3', index:'col3', width: "80"},
				{name:'col4', index:'col4', width: "50"},
				{name:'col5', index:'col5', width: "55"},
				{name:'col6', index:'col6', width: "60"},
				{name:'col7', index:'col7', width: "120"},
				{name:'col8', index:'col8', width: "60"},
				{name:'col9', index:'col9', width: "60"},
				{name:'col10', index:'col10', width: "80"},
				{name:'col11', index:'col11', width: "80"},
				{name:'col12', index:'col12', width: "80"}
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
			{col1:"양산",col2:"도면",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"N"},
			{col1:"양산",col2:"SW",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"Y"},
			{col1:"양산",col2:"문서",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"N"},
			{col1:"양산",col2:"도면",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"N"},
			{col1:"양산",col2:"문서",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"N"},
			{col1:"양산",col2:"도면",col3:"A200344",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"N"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="">출력요청사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
		<li>
			<label for="">출력반려사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">출력승인 목록</span><span class="listCount">6</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">Viewing</button>
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
		<button class="ui-button ui-corner-all bottomBtn">출력승인</button>
		<button class="ui-button ui-corner-all bottomBtn">출력반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>