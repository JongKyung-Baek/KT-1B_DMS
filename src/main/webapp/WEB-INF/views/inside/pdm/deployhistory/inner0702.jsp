<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 대결자 (decorator type = decoratorTree) -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>대결자 - 설정 - CollabHub</title>
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
			colNames:['소속부서','사용자ID','사용자명','직급','위임상태'],
			colModel:[
				{name:'col1', index:'col1', width:'180'},
				{name:'col2', index:'col2', width:'150'},
				{name:'col3', index:'col3', width:'120'},
				{name:'col4', index:'col4', width:'100'},
				{name:'col5', index:'col5', width:'80'},
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
			{col1:"영업팀",col2:"admin",col3:"관리자",col4:"팀장",col5:"Y"},
			{col1:"영업팀",col2:"inuser",col3:"홍길동",col4:"과장",col5:"Y"},
			{col1:"영업팀",col2:"inuser1",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser2",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser3",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser4",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser5",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser6",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser7",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser8",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser9",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser10",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser11",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser12",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser113",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser14",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser15",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser16",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser17",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser18",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser19",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser20",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser21",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser22",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser23",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser24",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser25",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser26",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser27",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser28",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser29",col3:"김길동",col4:"대리",col5:"N"},
			{col1:"영업팀",col2:"inuser20",col3:"김길동",col4:"대리",col5:"N"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGrid").jqGrid('addRowData',i+1,mydata[i]);
		}
		
		/* ----- container & jqgrid height(common_grid_paging.js) ----- */
		function containerHeight(){
			var tg1 = $('.bodyWrap .contentArea'); // contentArea height
			var tg1height = tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true);
			tg1.css({'height': tg1height + 'px'});
			
			$('.bodyWrap .treeArea').css({'height': tg1height + 'px'});	// treeArea height		
			
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

		var treeParam = {
			list: [
				{id: 'T_0000', text: '한화디펜스', parent: '#', level: 0},
				{id: 'T_0001', text: '영업팀', parent: 'T_0000', level: 1},
				{id: 'T_0002', text: '영업영업1팀', parent: 'T_0001', level: 2},
				{id: 'T_0003', text: '영업영업2팀', parent: 'T_0001', level: 2},
				{id: 'T_0004', text: '영업영업3팀', parent: 'T_0001', level: 2},
				{id: 'T_0005', text: '구매팀', parent: 'T_0000', level: 1},
				{id: 'T_0006', text: '구매구매1팀', parent: 'T_0005', level: 2},
				{id: 'T_0007', text: '구매구매2팀', parent: 'T_0005', level: 2},

				{id: 'T_0008', text: '생산팀', parent: 'T_0000', level: 1},
				{id: 'T_0009', text: '생산생산1팀', parent: 'T_0008', level: 2},
				{id: 'T_0010', text: '생산생산2팀', parent: 'T_0008', level: 2},
				{id: 'T_0011', text: '생산생산생산2팀', parent: 'T_0010', level: 3},
				{id: 'T_0012', text: '생산생산생산생산2팀', parent: 'T_0011', level: 4},
				{id: 'T_0013', text: '생산생산생산생산2팀', parent: 'T_0011', level: 4},
				{id: 'T_0014', text: '생산생산생산생산생산2팀', parent: 'T_0013', level: 5},
				{id: 'T_0015', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0016', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0017', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0018', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0019', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0020', text: '생산생산생산생산생산생산2팀', parent: 'T_0014', level: 6},
				{id: 'T_0021', text: '생산생산생산생산생산생산생산2팀', parent: 'T_0020', level: 7},
				{id: 'T_0022', text: '생산생산생산생산생산생산생산생산2팀', parent: 'T_0021', level: 8},
				{id: 'T_0023', text: '생산생산생산생산생산생산생산생산생산2팀', parent: 'T_0022', level: 9},
				
				{id: 'T_0024', text: '생산생산3팀', parent: 'T_0008', level: 2},
				{id: 'T_0025', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0026', text: '생산생산3팀', parent: 'T_0008', level: 2},
				{id: 'T_0027', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0028', text: '생산생산3팀', parent: 'T_0008', level: 2},
				{id: 'T_0029', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0030', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0031', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0032', text: '생산생산3팀', parent: 'T_0008', level: 2},
				{id: 'T_0033', text: '생산생산4팀', parent: 'T_0008', level: 2},
				{id: 'T_0034', text: '생산생산3팀', parent: 'T_0008', level: 2},
				{id: 'T_0035', text: '생산생산4팀', parent: 'T_0008', level: 2},
			]
		};

		console.log(treeParam);

		settingTree("jstree", treeParam);
	});


</script>
<style>
</style>
</head>
<body>
	<div class="br">
		<div class="treeArea">
			<div id="jstree"></div>
		</div>
	</div>
	<div class="br center"></div>
	<div class="br">
		<div class="contentArea">
			<div class="sbr">
				<ul class="ibx">
					<li>
						<label for="">사용자ID(사번)</label>
						<input type="text" id="">
					</li>
					<li>
						<label for="">사용자명</label>
						<input type="text" id="">
					</li>
					<li>
						<label for="">위임상태</label>
						<select id=""><option>전체</option></select>
					</li>
				</ul>
				<div class="btnBox">
					<button class="ui-button ui-corner-all searchBtn">조회</button>
				</div>
			</div>
			<div class="btnArea">
				<div class="left"></div>
				<div class="right">
					<button class="ui-button ui-corner-all">삭제</button>
					<button class="ui-button ui-corner-all">저장</button>
				</div>
			</div>
			<div class="gridArea whole">
				<div class="gridContainer">
					<table id="jqGrid"></table>
					<div id="pagerArea"></div>
					<div id="paginate"></div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>