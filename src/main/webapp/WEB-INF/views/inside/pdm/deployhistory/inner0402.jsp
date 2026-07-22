<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 배포승인 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배포승인 - 생산기술자료 - CollabHub</title>
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
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0402popup"}, "popupDialog", 'm', 520);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['요청번호','배포유형','배포일','요청자','요청일자','배포요청사유'],
			colModel:[
				{name:'col1', index:'col1', width:'150'},
				{name:'col2', index:'col2', width:'150'},
				{name:'col3', index:'col3', width:'150'},
				{name:'col4', index:'col4', width:'150'},
				{name:'col5', index:'col5', width:'150'},
				{name:'col6', index:'col6', width:'150'},
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
			{col1:"<a onclick='openDialog()'>A9827345</a>",col2:"신규",col3:"2019-01-01",col4:"홍길동",col5:"2019-01-01",col6:"배포요청사유사유사유"},
			{col1:"<a onclick='openDialog()'>A9827345</a>",col2:"기존대체",col3:"2019-01-01",col4:"홍길동",col5:"2019-01-01",col6:"배포요청사유사유사유"},
			{col1:"<a onclick='openDialog()'>A9827345</a>",col2:"추가",col3:"2019-01-01",col4:"홍길동",col5:"2019-01-01",col6:"배포요청사유사유사유"},
			{col1:"<a onclick='openDialog()'>A9827345</a>",col2:"신규",col3:"2019-01-01",col4:"홍길동",col5:"2019-01-01",col6:"배포요청사유사유사유"},
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
				<label for="">요청일</label>
				<div class="input-append date" id="fromDate">
					<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
				<span class="fromTo">~</span>
				<div class="input-append date" id="toDate">
					<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
			</li>
			<li>
				<label for="">요청번호</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">요청자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">배포유형</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">사업장</label>
				<select id=""><option>1사업장</option></select>
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
			<!-- <button class="ui-button ui-corner-all">일괄 승인</button>
			<button class="ui-button ui-corner-all">일괄 반려</button> -->
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog" class="dialogContainer"></div> <!-- 요청번호 상세보기 -->
</body>
</html>