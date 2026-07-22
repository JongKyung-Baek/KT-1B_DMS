<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 문서 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>문서 - 검색 및 배포요청 - CollabHub</title>
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
		$("#input11").prettyCheckable({labelPosition:'left'});
		$("#input12").prettyCheckable({labelPosition:'left'});
	});
	function openDialog1() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner010202popup1"}, "popupDialog1", 'm', 450);
	}
	function openDialog2() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner010202popup2"}, "popupDialog2", 'm', 380);
	}
	function openDialog3() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner010201popup3"}, "popupDialog3", 's', 380);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['사업구분','배포유형','문서분류','문서번호','REV','문서명','파일명','등록자','등록팀','등록일','규격화승인일','기술변경승인일','CO번호','업체명','구매담당자','기종','사업장','방산기술'],
			colModel:[
				{name:'col1', index:'col1', width:'80'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'120'},
				{name:'col4', index:'col4', width:'80'},
				{name:'col5', index:'col5', width:'80'},
				{name:'col6', index:'col6', width:'120'},
				{name:'col7', index:'col7', width:'120'},
				{name:'col8', index:'col8', width:'80'},
				{name:'col9', index:'col9', width:'80'},
				{name:'col10', index:'col10', width:'80'},
				{name:'col11', index:'col11', width:'80'},
				{name:'col12', index:'col12', width:'80'},
				{name:'col13', index:'col13', width:'80'},
				{name:'col14', index:'col14', width:'80'},
				{name:'col15', index:'col15', width:'90'},
				{name:'col16', index:'col16', width:'90'},
				{name:'col17', index:'col17', width:'80'},
				{name:'col18', index:'col18', width:'80'},
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
			{col1:"개발",col2:"원도",col3:"기술보고자료",col4:"",col5:"",col6:"",col7:"",col8:"홍길동",col9:"체계기술1팀",col10:"2006-09-26",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"LL54",col17:"1사업장",col18:"Y"},
			{col1:"양산",col2:"승인요청도",col3:"기술보고자료",col4:"",col5:"",col6:"",col7:"",col8:"홍길동",col9:"체계기술2팀",col10:"2006-09-26",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"LL54",col17:"2사업장",col18:"N"},
			{col1:"개발",col2:"승인도",col3:"소프트웨어",col4:"",col5:"",col6:"",col7:"123.plt",col8:"홍길동",col9:"체계기술1팀",col10:"2006-09-26",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"LL54",col17:"2사업장",col18:"N"},
			{col1:"양산",col2:"긴급도",col3:"QAR",col4:"",col5:"",col6:"",col7:"",col8:"홍길동",col9:"체계기술2팀",col10:"2006-09-26",col11:"",col12:"",col13:"",col14:"",col15:"",col16:"LL54",col17:"1사업장",col18:"Y"},
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
				<label for="">등록일</label>
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
				<label for="">문서명</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">배포유형</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">CO번호</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">기종</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">사업장</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">구매담당자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">업체명</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">등록자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<input type="checkbox" name="input12" id="input12">
				<label for="input12">최종 Rev.</label>
			</li>
		</ul>
		<div class="btnBox">
			<button class="ui-button ui-corner-all searchBtn">조회</button>
		</div>
	</div>
	<div class="btnArea">
		<div class="left">
			<button class="ui-button ui-corner-all">엑셀</button>
			<button class="ui-button ui-corner-all" onclick="openDialog3()">일괄검색</button>
		</div>
		<div class="right">
			<button class="ui-button ui-corner-all">Viewing</button>
			<button class="ui-button ui-corner-all" onclick="openDialog1()">배포승인요청</button>
			<button class="ui-button ui-corner-all" onclick="openDialog2()">출력승인요청</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog1" class="dialogContainer"></div> <!-- 배포승인요청 버튼 -->
	<div id="popupDialog2" class="dialogContainer"></div> <!-- 출력승인요청 버튼 -->
	<div id="popupDialog3" class="dialogContainer"></div> <!-- 일괄검색 버튼 -->
</body>
</html>