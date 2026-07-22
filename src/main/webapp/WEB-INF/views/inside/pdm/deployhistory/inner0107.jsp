<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 출력이력 - 자료배포 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>출력이력 - CollabHub</title>
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
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0107popup"}, "popupDialog", 'm', 300);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['사업구분','자료유형','자료번호','REV','SW버전','명세서버전','자료명','페이지','총페이지','CO번호','용도','출력요청자','출력요청일','출력횟수','출력유효기간','출력폐기기한','규격화승인일','기술변경승인일','기종','사업장','방산기술자료','출력요청사유','폐기요청자','폐기승인일','폐기일자'],
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
			{col1:"양산",col2:"도면",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"1/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"N",col22:"",col23:"",col24:"",col25:""},
			{col1:"양산",col2:"문서",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"1/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"N",col22:"",col23:"",col24:"",col25:""},
			{col1:"양산",col2:"SW",col3:"A20034",col4:"",col5:"0",col6:"",col7:"",col8:"",col9:"",col10:"",col11:"",col12:"",col13:"",col14:"3/3",col15:"",col16:"",col17:"",col18:"",col19:"",col20:"",col21:"Y",col22:"",col23:"",col24:"",col25:""}
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGrid").jqGrid('addRowData',i+1,mydata[i]);
		}
		
		/* ----- btnArea textCaution / script 적용 순서 주의 ----- */
		var tcl = $('.contentArea .left .textCaution').parent('.left');
		tcl.siblings('.right').css({'width':'auto'});
		tcl.css({'width':'calc(100% - ' + tcl.siblings('.right').outerWidth(true) + 'px'});			
		/* ----- container & jqgrid height(common_grid_paging.js) / script 적용 순서 주의  ----- */
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
				<label for="">자료유형</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">출력요청자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">용도</label>
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
			<span class="textCaution">출력회수 : 3회 , 출력유효기한 : 팀장 승인 후 1주일</span>
		</div>
		<div class="right">
			<button class="ui-button ui-corner-all">Viewing</button>
			<button class="ui-button ui-corner-all" onclick="openDialog()">출력물폐기요청</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog" class="dialogContainer"></div> <!-- 출력물폐기요청 버튼 -->
</body>
</html>