<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포이력 팝업(거래중단 버튼)-->
<script>
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업자번호','업체명'],
			colModel:[
				{name:'col1', index:'col1', width: "200"},
				{name:'col2', index:'col2', width: "150"}
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
			{col1:"11-1111-3333",col2:"관리자"},
			{col1:"11-1111-3333",col2:"홍길동"},
			{col1:"11-1111-3333",col2:"홍길동"}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<p class="textCaution">거래중단을 할 경우 해당업체는 Viewing/파일다운로드를 할 수 없습니다.</p>
	<div class="sbr">
		<ul class="ibx">
			<li>
				<label for="">사업자번호</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">업체명</label>
				<input type="text" id="">
			</li>
		</ul>
		<div class="btnBox">
			<button class="ui-button ui-corner-all searchBtn">조회</button>
		</div>
	</div>
	<div class="section">
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">거래중단</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>