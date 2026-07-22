<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 사용자 관리 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>사용자 관리 - 사용자 관리 - CollabHub</title>
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
	});
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['사용자계정','사용자성명','부서','직위','최종로그인','최종비밀번호변경일자','계정잠금여부','비밀번호틀린횟수'],
			colModel:[
				{name:'col1', index:'col1', width:'100'},
				{name:'col2', index:'col2', width:'100'},
				{name:'col3', index:'col3', width:'150'},
				{name:'col4', index:'col4', width:'100'},
				{name:'col5', index:'col5', width:'100'},
				{name:'col6', index:'col6', width:'100'},
				{name:'col7', index:'col7', width:'100'},
				{name:'col8', index:'col8', width:'100'}
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
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
			{col1:"i1234567",col2:"홍길동",col3:"경영지원실 구매팀",col4:"과장",col5:"2019-03-21",col6:"",col7:"N",col8:"0"},
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
				<label for="">사용자계정</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">사용자성명</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">부서</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">제작구분</label>
				<select id=""><option>재직자</option></select>
			</li>
		</ul>
		<div class="btnBox">
			<button class="ui-button ui-corner-all searchBtn">조회</button>
		</div>
	</div>
	<div class="btnArea">
		<div class="left"></div>
		<div class="right">
			<button class="ui-button ui-corner-all">계정잠금해제</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
</body>
</html>