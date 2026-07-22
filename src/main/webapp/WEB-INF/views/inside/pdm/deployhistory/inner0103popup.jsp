<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포요청현황 팝업(요청번호 상세보기)-->
<script>
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업구분','배포유형','자료유형','배포방식','자료번호','REV','S/W버전','명세서버전','자료명','페이지','총페이지','규격화승인일','기술변경승인일','기종','사업장','방산기술'],
			colModel:[
				{name:'col1', index:'col1', width: "50"},
				{name:'col2', index:'col2', width: "50"},
				{name:'col3', index:'col3', width: "50"},
				{name:'col4', index:'col4', width: "50"},
				{name:'col5', index:'col5', width: "50"},
				{name:'col6', index:'col6', width: "50"},
				{name:'col7', index:'col7', width: "50"},
				{name:'col8', index:'col8', width: "60"},
				{name:'col9', index:'col9', width: "50"},
				{name:'col10', index:'col10', width: "50"},
				{name:'col11', index:'col11', width: "50"},
				{name:'col12', index:'col12', width: "80"},
				{name:'col13', index:'col13', width: "80"},
				{name:'col14', index:'col14', width: "50"},
				{name:'col15', index:'col15', width: "50"},
				{name:'col16', index:'col16', width: "50"}
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
			{col1:"양산",col2:"승인도",col3:"도면",col4:"일반영역",col5:"A200344",col6:"",col7:"0",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"N"},
			{col1:"양산",col2:"승인도",col3:"SW",col4:"Viewing",col5:"A200344",col6:"",col7:"0",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"Y"},
			{col1:"양산",col2:"승인도",col3:"문서",col4:"Viewing",col5:"A200344",col6:"",col7:"0",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"N"},
			{col1:"양산",col2:"승인도",col3:"도면",col4:"보안영역",col5:"A200344",col6:"",col7:"0",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"N"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="">변경요청사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
		<li>
			<label for="">배포요청 반려 사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포승인 목록</span><span class="listCount">4</span>
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
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>