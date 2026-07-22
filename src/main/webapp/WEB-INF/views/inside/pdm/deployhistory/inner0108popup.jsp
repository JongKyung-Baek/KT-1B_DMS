<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 출력물 폐기승인 팝업(요청번호 상세보기)-->
<script>
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업구분','자료유형','자료번호','REV','SW버전','명세서버전','자료명','페이지','총페이지','CO번호','용도','요청자','출력요청일','출력횟수','출력유효기간','출력물폐기기한','규격화승인일','기술변경승인일','기종','사업장','방산기술자료','출력요청사유','폐기요청자','폐기승인자','폐기일자'],
			colModel:[
				{name:'col1', index:'col1', width:'80'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'80'},
				{name:'col4', index:'col4', width:'80'},
				{name:'col5', index:'col5', width:'80'},
				{name:'col6', index:'col6', width:'80'},
				{name:'col7', index:'col7', width:'80'},
				{name:'col8', index:'col8', width:'80'},
				{name:'col9', index:'col9', width:'80'},
				{name:'col10', index:'col10', width:'80'},
				{name:'col11', index:'col11', width:'80'},
				{name:'col12', index:'col12', width:'80'},
				{name:'col13', index:'col13', width:'80'},
				{name:'col14', index:'col14', width:'80'},
				{name:'col15', index:'col15', width:'80'},
				{name:'col16', index:'col16', width:'80'},
				{name:'col17', index:'col17', width:'80'},
				{name:'col18', index:'col18', width:'80'},
				{name:'col19', index:'col19', width:'80'},
				{name:'col20', index:'col20', width:'80'},
				{name:'col21', index:'col21', width:'80'},
				{name:'col22', index:'col22', width:'80'},
				{name:'col23', index:'col23', width:'80'},
				{name:'col24', index:'col24', width:'80'},
				{name:'col25', index:'col25', width:'80'}
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
			{col1:"양산",col2:"도면",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"1/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"N",col22:"",col23:"",col24:"",col25:""},
			{col1:"양산",col2:"문서",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"1/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"N",col22:"",col23:"",col24:"",col25:""},
			{col1:"양산",col2:"문서",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"1/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"N",col22:"",col23:"",col24:"",col25:""},
			{col1:"양산",col2:"SW",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"3/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"Y",col22:"",col23:"",col24:"",col25:""}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="">출력폐기사유</label>
			<div><textarea id="" rows="2" disabled></textarea></div>
		</li>
		<li>
			<label for="">출력폐기반려사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">출력물 폐기 승인 목록</span><span class="listCount">4</span>
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
		<button class="ui-button ui-corner-all bottomBtn">폐기승인</button>
		<button class="ui-button ui-corner-all bottomBtn">폐기반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>