<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 배포이력 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배포이력 - 생산기술자료 - CollabHub</title>
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#fromDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: true,
			language: 'ko',
			pickTime: true,
		}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });
		$("#toDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: true,
			language: 'ko',
			pickTime: true,
		}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });
	});

	function openDialog() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0406popup"}, "popupDialog", 's', 310);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['배포유형','기종','문서번호','자료번호','REV','문서명','사업장','배포부서','배포자','배포일','배포매수','접수부서','접수자','접수일','유효기간','접수매수','폐기부서','폐기자','폐기일','폐기매수'],
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
			{col1:"신규",col2:"작업표준서",col3:"",col4:"6720MXX",col5:"0",col6:"용단품가공",col7:"1사업장",col8:"생산기술",col9:"홍길동",col10:"2019-01-01",col11:"79",col12:"가공직",col13:"<a onclick='openDialog()'>김길동 외 3명</a>",col14:"2019-01-01",col15:"2020-01-01",col16:"95",col17:"",col18:"",col19:"",col20:""},
			{col1:"기존대체",col2:"작업표준서",col3:"",col4:"6720MXX",col5:"0",col6:"용단품가공",col7:"1사업장",col8:"생산기술",col9:"홍길동",col10:"2019-01-01",col11:"",col12:"가공직",col13:"<a onclick='openDialog()'>김길동</a>",col14:"2019-01-01",col15:"2020-01-01",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"추가",col2:"작업표준서",col3:"",col4:"6720MXX",col5:"0",col6:"용단품가공",col7:"2사업장",col8:"생산기술",col9:"홍길동",col10:"2019-01-01",col11:"10",col12:"가공직",col13:"<a onclick='openDialog()'>홍길동 외 2명</a>",col14:"2019-01-01",col15:"2020-01-01",col16:"10",col17:"",col18:"",col19:"",col20:""},
			{col1:"기존대체",col2:"작업표준서",col3:"",col4:"6720MXX",col5:"0",col6:"용단품가공",col7:"2사업장",col8:"생산기술",col9:"홍길동",col10:"2019-01-01",col11:"7",col12:"가공직",col13:"<a onclick='openDialog()'>배길동</a>",col14:"2019-01-01",col15:"2020-01-01",col16:"7",col17:"",col18:"",col19:"",col20:""},
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
				<label for="">접수일</label>
				<div class="input-append date" id="fromDate">
					<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
				<span class="fromTo">~</span>
				<div class="input-append date" id="toDate">
					<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
			</li>
			<li>
				<label for="">문서번호</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">요청자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">접수자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">배포자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">부서</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">업무유형</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">사업장</label>
				<select id=""><option>1사업장</option></select>
			</li>
			<li>
				<label for="">자료유형</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">배포유형</label>
				<select id=""><option>전체</option></select>
			</li>
		</ul>
		<div class="btnBox">
			<button class="ui-button ui-corner-all searchBtn">조회</button>
		</div>
	</div>
	<div class="btnArea">
		<div class="left">
			<button class="ui-button ui-corner-all ">엑셀</button>
		</div>
		<div class="right">
			<button class="ui-button ui-corner-all">출력</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog" class="dialogContainer"></div> <!-- 접수자 상세보기 -->
</body>
</html>