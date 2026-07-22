<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 업체 및 사용자 관리 (decorator type = decoratorHalf) -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>업체 및 사용자 관리 - 사용자 관리 - CollabHub</title>
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

	function openDialog1() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0502popup1"}, "popupDialog1", 's', 240);
	}
	function openDialog2() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0502popup2"}, "popupDialog2", 's', 280);
	}
	function openDialog3() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0502popup3"}, "popupDialog3", 's', 240);
	}
	function openDialog4() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0502popup4"}, "popupDialog4", 's', 280);
	}
</script>
<script>
	$(document).ready(function(){
		var gridObj = {
				datatype: "local",
				colNames:['업체명','구매담당자','사업자번호'],
				colModel:[
					{name:'col1', index:'col1', width:'200'},
					{name:'col2', index:'col2', width:'150'},
					{name:'col3', index:'col3', width:'200'},
				],
				width : null,
				height : null,
				autoheight : true,
				rowNum : 15,
				rownumbers : true,
				multiselect : false,
				caption : false,
				loadtext : /*'<img src=''/>'*/ 'loading~~',
				pager : '#pagerArea1',
			    rowList: [15,30,50,100],        // disable page size dropdown
			    pgbuttons: true,
	            viewsortcols : [ false, 'horizontal', true ],
			    viewrecords: true,
			    loadComplete: function(data) {
			    	initPage("jqGrid1", "pagerArea1", true, "TOT");
			    }
			};
		
		$("#jqGrid1").jqGrid(gridObj);
		var mydata1 = [
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동",col3:"123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동홍길동",col3:"123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동",col3:"123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트이솝소프트</a>",col2:"홍길동",col3:"123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동",col3:"123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동",col3:"123456789123456789"},
			{col1:"<a onclick='openDialog3()'>(주)이솝소프트</a>",col2:"홍길동",col3:"123456789"},
			];
		for(var i=0;i<=mydata1.length;i++){
			$("#jqGrid1").jqGrid('addRowData',i+1,mydata1[i]);
		}
		
		var gridObj2 = {
				datatype: "local",
				colNames:['업체사용자','이메일','최종로그인일자','최종비밀번호변경일자','계정잠금여부','비밀번호틀린횟수'],
				colModel:[
					{name:'col1', index:'col1', width:'100'},
					{name:'col2', index:'col2', width:'180'},
					{name:'col3', index:'col3', width:'150'},
					{name:'col4', index:'col4', width:'100'},
					{name:'col5', index:'col5', width:'100'},
					{name:'col6', index:'col6', width:'100'},
				],
				width : null,
				height : null,
				autoheight : true,
				rowNum : 15,
				rownumbers : true,
				multiselect : true,
				caption : false,
				loadtext : /*'<img src=''/>'*/ 'loading~~',
				pager : '#pagerArea2',
			    rowList: [15,30,50,100],        // disable page size dropdown
			    pgbuttons: true,
	            viewsortcols : [ false, 'horizontal', true ],
			    viewrecords: true,
			    loadComplete: function(data) {
			    	initPage("jqGrid2", "pagerArea2", true, "TOT");
			    }
			}
		
		
		$("#jqGrid2").jqGrid(gridObj2);
		var mydata2 = [
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			{col1:"<a onclick='openDialog4()'>홍길동</a>",col2:"esob@esob.kr",col3:"2019-01-01 12:35",col4:"",col5:"N",col6:"0"},
			];
		for(var i=0;i<=mydata2.length;i++){
			$("#jqGrid2").jqGrid('addRowData',i+1,mydata2[i]);
		}
		
		/* ----- container & jqgrid height(common_grid_paging.js) ----- */
		function containerHeight(){
					
			var tg1 = $('.bodyWrap .contentArea'); // contentArea height
			var tg1height = tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true);
			tg1.css({'height': tg1height + 'px'});	
			$('.bodyWrap .contentArea.half > .br > .gridArea').each(function(index){
				var tg = $(this);  // gridArea height
				tg.css({'height': $('.contentArea.half').height() - tg.siblings('.sbr').outerHeight(true) - tg.siblings('.btnArea').outerHeight(true) - 10 + 'px'});				
				var tg3 = tg.find('.ui-jqgrid .ui-jqgrid-view'); // ui-jqgrid-view height
				tg3.css({'height' : tg.height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});				
				var tg4 = tg.find('.ui-jqgrid-bdiv'); // ui-jqgrid-bdiv height
				tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 			
			});	
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
	<div class="br">
		<div class="sbr">
			<ul class="ibx">
				<li>
					<label for="">사업자번호</label>
					<input type="text" name="" id="">
				</li>
				<li>
					<label for="">업체명</label>
					<input type="text" name="" id="">
				</li>
			</ul>
			<div class="btnBox">
				<button class="ui-button ui-corner-all searchBtn">조회</button>
			</div>
		</div>
		<div class="btnArea">
			<div class="left">
				<button class="ui-button ui-corner-all">엑셀(업체)</button>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all" onclick="openDialog1()">업체등록</button>
			</div>
		</div>
		<div class="gridArea">
			<div class="gridContainer">
				<table id="jqGrid1"></table>
				<div id="pagerArea1"></div>
				<div id="paginate1"></div>
			</div>
		</div>
	</div>
	<div class="br center"></div>
	<div class="br">
		<div class="sbr">
			<ul class="ibx">
				<li>
					<label for="">업체사용자</label>
					<input type="text" name="" id="">
				</li>
				<li>
					<label for="">업체사용자</label>
					<input type="text" name="" id="">
				</li>
				<li>
					<label for="">업체사용자</label>
					<input type="text" name="" id="">
				</li>
			</ul>
			<div class="btnBox">
				<button class="ui-button ui-corner-all searchBtn">조회</button>
			</div>
		</div>
		<div class="btnArea">
			<div class="left">
				<!-- <button class="ui-button ui-corner-all">엑셀(업체사용자)</button> -->
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">계정잠금해제</button>
				<button class="ui-button ui-corner-all" onclick="openDialog2()">업체사용자등록</button>
				<!-- <button class="ui-button ui-corner-all">삭제</button> -->
			</div>
		</div>
		<div class="gridArea">
			<div class="gridContainer">
				<table id="jqGrid2"></table>
				<div id="pagerArea2"></div>
				<div id="paginate2"></div>
			</div>
		</div>
	</div>
	<div id="popupDialog1" class="dialogContainer"></div> <!-- 업체등록 버튼 -->
	<div id="popupDialog2" class="dialogContainer"></div> <!-- 업체사용자등록 버튼 -->
	<div id="popupDialog3" class="dialogContainer"></div> <!-- 업체 상세보기 -->
	<div id="popupDialog4" class="dialogContainer"></div> <!-- 업체사용자 상세보기 -->
</body>
</html>