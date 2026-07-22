<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 배포승인현황 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배포 승인현황 - 문서 - CollabHub</title>
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
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"external0202popup1"}, "popupDialog1", 's', 500);
	}
	function openDialog2() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"external0202popup2"}, "popupDialog2", 's', 330);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['사업구분','배포유형','승인번호','문서번호','문서명','파일명','REV','용도','구매담당자','유효기간','요청일','승인일','기종','방산기술','폐기구분','첨부파일'],
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
				{name:'col14', index:'col6', width:'80'},
				{name:'col15', index:'col15', width:'80'},
				{name:'col16', index:'col16', width:'80'},
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
			{col1:"양산",col2:"VIEWING",col3:"42354",col4:"23452",col5:"조립도",col6:"ASFE.PLT",col7:"A",col8:"검토용",col9:"김구매",col10:"2019-01-01",col11:"2019-01-01",col12:"2019-01-01",col13:"6701",col14:"",col15:"폐기중",col16:"<a onclick='openDialog2()'>1건</a>"},
			{col1:"양산",col2:"VIEWING",col3:"89648",col4:"123456",col5:"경보기",col6:"AAA.HWP",col7:"A",col8:"검토용",col9:"김구매",col10:"2019-01-01",col11:"2019-01-01",col12:"2019-01-01",col13:"6701",col14:"",col15:"폐기완료",col16:"<a onclick='openDialog2()'>1건</a>"},
			{col1:"양산",col2:"일반배포",col3:"42354",col4:"23452",col5:"사격펄스측정장비",col6:"ASD.ZIP",col7:"A",col8:"검토용",col9:"김구매",col10:"2019-01-01",col11:"2019-01-01",col12:"2019-01-01",col13:"6701",col14:"",col15:"폐기중",col16:"<a onclick='openDialog2()'>1건</a>"},
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
				<label for="input5">요청일</label>
				<div class="input-append date" id="fromDate">
					<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
				<span class="fromTo">~</span>
				<div class="input-append date" id="toDate">
					<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
			</li>
			<li>
				<label for="input1">문서번호</label>
				<input type="text" name="" id="input1">
			</li>
			<li>
				<label for="input2">문서명</label>
				<input type="text" name="" id="input2">
			</li>
			<li>
				<label for="input3">구매담당자</label>
				<select id="input3"><option>전체</option></select>
			</li>
			<li>
				<label for="input4">업체담당자</label>
				<select id="input4"><option>이왕근</option></select>
			</li>
			<li>
				<input type="checkbox" id="input6">
				<label for="input6">유효기간 초과</label>
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
			<button class="ui-button ui-corner-all">Viewing</button>
			<button class="ui-button ui-corner-all">출력</button>
			<button class="ui-button ui-corner-all" onclick="openDialog()">파일다운</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog1" class="dialogContainer"></div> <!-- 파일다운 버튼 -->
	<div id="popupDialog2" class="dialogContainer"></div> <!-- 첨부파일 상세보기 -->
</body>
</html>