<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 배포요청접수 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배포요청접수 - 자료배포 - CollabHub</title>
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#fromDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: true,
			language: 'ko',
			pickTime: false,
		}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });
		$("#toDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: true,
			language: 'ko',
			pickTime: false,
		}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });
	});
	
	function openDialog() {
		openDialogPopup("/inside/pdm/deployhistory/approvePopup", {test: 111, test2:222}, "approvePopup", 'm', 600);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['요청번호','자료유형','대표번호','용도','구매담당자','요청업체','요청일','승인일','사업장','처리상태','처리자','방산기술'],
			colModel:[
				{name:'col1', index:'col1', width:'120'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'80'},
				{name:'col4', index:'col4', width:'80'},
				{name:'col5', index:'col5', width:'90'},
				{name:'col6', index:'col6', width:'90'},
				{name:'col7', index:'col7', width:'80'},
				{name:'col8', index:'col8', width:'80'},
				{name:'col9', index:'col9', width:'80'},
				{name:'col10', index:'col10', width:'80'},
				{name:'col11', index:'col11', width:'80'},
				{name:'col12', index:'col12', width:'80'},
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
			pager : '#pagerArea',
		    rowList: [15,30,50,100],        // disable page size dropdown
		    pgbuttons: true,
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true,
		    loadComplete: function(data) {
		    	initPage("jqGrid", "pagerArea", true, "TOT");
		    }
		});
		var mydata = [
			{col1:"<a onclick='openDialog()'>20161233-D0014</a>",col2:"도면",col3:"A200011234",col4:"견적용",col5:"홍길동",col6:"와우소프트㈜",col7:"2019-03-01",col8:"2019-03-01",col9:"1사업장",col10:"승인",col11:"홍길동",col12:"Y"},
			{col1:"<a onclick='openDialog()'>20161233-D0013</a>",col2:"문서",col3:"A200011233",col4:"검토용",col5:"홍길동",col6:"와우소프트㈜",col7:"2019-02-01",col8:"2019-02-01",col9:"2사업장",col10:"반려",col11:"김길동",col12:"N"},
			{col1:"<a onclick='openDialog()'>20161233-D0012</a>",col2:"SW",col3:"A200011232",col4:"교범용",col5:"홍길동",col6:"와우소프트㈜",col7:"2019-01-01",col8:"2019-01-01",col9:"1사업장",col10:"미결재",col11:"김길동",col12:"Y"},
			{col1:"<a onclick='openDialog()'>20161233-D0013</a>",col2:"문서",col3:"A200011233",col4:"제조용",col5:"홍길동",col6:"와우소프트㈜",col7:"2019-02-01",col8:"2019-02-01",col9:"2사업장",col10:"결재요청",col11:"김길동",col12:"N"},
			{col1:"<a onclick='openDialog()'>20161233-D0012</a>",col2:"SW",col3:"A200011232",col4:"참고용",col5:"홍길동",col6:"와우소프트㈜",col7:"2019-01-01",col8:"2019-01-01",col9:"1사업장",col10:"미결재",col11:"김길동",col12:"Y"}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGrid").jqGrid('addRowData',i+1,mydata[i]);
		}
		
		/* ----- container & jqgrid height(common_grid_paging.js) ----- */
		function containerHeight(){
			var tg1 = $('.bodyWrap .contentArea'); // contentArea height
			var tg1height = tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true);
			tg1.css({'height': tg1height + 'px'});
			var tg2 = $('.bodyWrap .gridArea'); // gridArea height
			var tg2height = tg2.parents('.contentArea').height() - tg2.siblings('.sbr').outerHeight(true) - tg2.siblings('.btnArea').outerHeight(true) - 10;
			tg2.css({'height': tg2height + 'px'});
			var tg3 = $('.bodyWrap .ui-jqgrid .ui-jqgrid-view'); // ui-jqgrid-view height
			tg3.css({'height' : tg3.parent().parent('.gridContainer').height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});
			var tg4 = $('.ui-jqgrid .ui-jqgrid-bdiv'); // ui-jqgrid-bdiv height
			tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 
		}
		containerHeight(); // Set height value when loading page
		$(window).resize(function(){ // Set height value when resizing pages
			containerHeight();
		});	
	});
</script>
<style>
</style>
</head>
<body>
	<div class="sbr">
		<ul class="ibx">
			<li>
				<label for="input7">요청일</label>
				<div class="input-append date" id="fromDate">
					<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
				<span class="fromTo">~</span>
				<div class="input-append date" id="toDate">
					<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
			</li>
			<li>
				<label for="input2">자료유형</label>
				<select id="input2"><option>전체</option><option>도면</option><option>문서</option><option>SW</option></select>
			</li>
			<li>
				<label for="input1">요청업체</label>
				<select id="input1"><option>전체</option></select>
			</li>
			<li>
				<label for="input5">용도</label>
				<select id="input5"><option>전체</option><option>검토용</option><option>견적용</option><option>교범용</option><option>제조용</option><option>참고용</option></select>
			</li>
			<li>
				<label for="input6">처리상태</label>
				<select id="input6"><option>전체</option><option>미결재</option><option>결재요청</option><option>반려</option><option>승인</option></select>
			</li>
			<li>
				<label for="input3">사업장</label>
				<select id="input3"><option>전체</option><option>1사업장</option><option>2사업장</option></select>
			</li>
			<li>
				<label for="input4">구매담당자</label>
				<select id="input4"><option>전체</option><option>이길동</option></select>
			</li>
		</ul>
		<div class="btnBox">
			<button class="ui-button ui-corner-all searchBtn">조회</button>
		</div>
	</div>
	<div class="btnArea"></div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="approvePopup" class="dialogContainer"></div> <!-- 요청번호 상세보기 -->
</body>
</html>