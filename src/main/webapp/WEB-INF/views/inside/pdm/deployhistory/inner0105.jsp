<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 배포이력 - 자료배포 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>배포이력 - CollabHub</title>
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
	function openDialog1() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0105popup1"}, "popupDialog1", 's', 400);
	}
	function openDialog2() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"inner0105popup2"}, "popupDialog2", 's', 520);
	}
</script>
<script>
	$(document).ready(function(){
		$("#jqGrid").jqGrid({
			datatype: "local",
			colNames:['승인번호','사업구분','배포유형','자료유형','배포방식','용도','자료번호','REV','최종REV','SW버전','명세서버전','자료명','페이지','총페이지','CO번호','기종','사업장','방산기술','업체명','구매담당자','승인자','승인일','배포요청사유','유효기간','폐기구분'],
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
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"문서",col2:"개발",col3:"긴급도",col4:"문서",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"SW",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"문서",col2:"개발",col3:"긴급도",col4:"문서",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"SW",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"문서",col2:"개발",col3:"긴급도",col4:"문서",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"SW",col5:"보안영역",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
			{col1:"도면",col2:"양산",col3:"승인도",col4:"도면",col5:"View",col6:"제조",col7:"A123",col8:"A",col9:"B",col10:"",col11:"",col12:"배선장치",col13:"",col14:"",col15:"",col16:"VT26",col17:"",col18:"",col19:"",col20:"",col21:"",col22:"",col23:"",col24:"",col25:""},
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
				<label for="">승인일</label>
				<div class="input-append date" id="fromDate">
					<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
				<span class="fromTo">~</span>
				<div class="input-append date" id="toDate">
					<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
				</div>
			</li>
			<li>
				<label for="">업체명</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">자료번호</label>
				<input type="text" id="">
			</li>
			<li>
				<label for="">유효기간경과</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">사업장</label>
				<select id=""><option>1사업장</option></select>
			</li>
			<li>
				<label for="">구매담당자</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">승인상태</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">기종</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">자료구분</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">사업구분</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">배포유형</label>
				<select id=""><option>전체</option></select>
			</li>
			<li>
				<label for="">배포방식</label>
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
			<button class="ui-button ui-corner-all" onclick="openDialog1()">거래중단</button>
			<button class="ui-button ui-corner-all" onclick="openDialog2()">폐기</button>
		</div>
	</div>
	<div class="gridArea whole">
		<div class="gridContainer">
			<table id="jqGrid"></table>
			<div id="pagerArea"></div>
			<div id="paginate"></div>
		</div>
	</div>
	<div id="popupDialog1" class="dialogContainer"></div> <!-- 거래중단 버튼 -->
	<div id="popupDialog2" class="dialogContainer"></div> <!-- 폐기 버튼 -->
</body>
</html>