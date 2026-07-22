<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 현장관리자 접수 팝업(접수 버튼)-->
<script>
	$(function() {		
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['요청번호','자료유형','배포유형','기종','문서번호','자료번호','REV','문서명','보유매수','접수매수','폐기매수','접수일'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2', width: "80"},
				{name:'col3', index:'col3', width: "80"},
				{name:'col4', index:'col4', width: "80"},
				{name:'col5', index:'col5', width: "80"},
				{name:'col6', index:'col6', width: "80"},
				{name:'col7', index:'col7', width: "80"},
				{name:'col8', index:'col8', width: "80"},
				{name:'col9', index:'col9', width: "80"},
				{name:'col10', index:'col10', width: "80"},
				{name:'col11', index:'col11', width: "80"},
				{name:'col12', index:'col12', width: "80"},
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
			{col1:"1903-001",col2:"용접공정서",col3:"신규",col4:"XK56",col5:"XXXXXXXX",col6:"K56W0900-1",col7:"0",col8:"Cargo-1",col9:"0",col10:"20",col11:"0",col12:"2019.03.05"},
			{col1:"1903-001",col2:"용접공정서",col3:"기존대체",col4:"XK56",col5:"XXXXXXXX",col6:"K56W0900-2",col7:"A",col8:"Cargo-1",col9:"100",col10:"25",col11:"23",col12:"2019.03.05"}
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
				<span class="sectTitle">접수 자료 목록</span><span class="listCount">2</span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
	<ul class="section">
		<li>
			<p class="sectTitle">배포 확인 및 전달 교육 결과</p>
		</li>
		<li>
			<label for="">교육 사유</label>
			<div><input type="text" id="" value="설계 변경 및 공정 개선(ECN000019)"></div>
		</li>
		<li>
			<label for="">교육 일자</label>
			<div><input type="text" id="" value="창원 1사업장"></div>
		</li>
		<li>
			<label for="">교육대상</label>
			<div><input type="text" id="" value="PLANO MILLER 작업자 2명"></div>
		</li>
		<li>
			<label for="">교육 결과</label>
			<div><textarea id="" rows="2">공정서 변경 내용 확인 Program 및 형상 변경 확인</textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">접수완료</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>