<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>도면 - 검색 및 배포요청 - 자료배포 - CollabHub</title>
<script type="text/javascript" src="/resources/js/gridscript.js"></script>
<script type="text/javascript" src="/resources/js/select2-master/dist/js/select2.js"></script>
<link type="text/css" rel="stylesheet" href="/resources/bootstrap/css/bootstrap-combined.min.css" media="screen" /> <!-- -->
<link type="text/css" rel="stylesheet" href="/resources/bootstrap/css/bootstrap-datetimepicker.min.css" media="screen" />
<link type="text/css" rel="stylesheet" href="/resources/bootstrap/css/bootstrap.min.css" media="screen" />
<link rel="stylesheet" type="text/css" href="/resources/prettyCheck/prettyCheckable.css" />
<link type="text/css" rel="stylesheet" href="/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
<link type="text/css" rel="stylesheet" href="/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
<link type="text/css" rel="stylesheet" href="/resources/js/select2-master/dist/css/select2.css" media="screen" />
<link type="text/css" rel="stylesheet" href="/resources/css/style.css" media="screen" />
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});

		
		$("#fromDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: 1,
			language: 'ko',
			pickTime: 1,
			viewMode : 'days',
			startDate : '2019-09-01',
			endDate : '2019-09-03',
			closeOnDateSelect: true
			
		});
		
		$('#fromDate').on('dp.change', function(e){ console.log(e.date); })
// 		.on('dp.change', function(ev){
// 			console.log(ev);
// 			console.log(ev.action);
// 			 if (ev.date.valueOf() < date-start-display.valueOf()){
// 			      console.log("gg");
// 			    }
// // 			if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } 
// 			});

		$('#fromDate').datetimepicker('setEndDate', '2019-09-03');
		
		
		$("#toDate").datetimepicker({
			format: 'yyyy-MM-dd',
			autoclose: true,
			language: 'ko',
			pickTime: false,
		}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });

		$("#input10").prettyCheckable({labelPosition:'left'});
		$("#input11").prettyCheckable({labelPosition:'left'});

	});
</script>
<style>
	.ui-jqgrid th.ui-th-column div{height:auto !important; padding:2px; white-space:normal !important;}
	.ui-jqgrid tr.ui-row-ltr td{text-align:center;}

</style>
</head>
<body>
<div class="container"> <!-- half whole -->
	<div class="nav">
		<h3 class="titleBox"><span>도면</span></h3>
		<p class="navBox"><span>자료배포</span><span>검색 및 배포요청</span></p>
	</div>
	<div class="contentArea whole">
		<div class="sbr">
			<ul class="ibx">
				<li>
					<label for="input1">도면번호</label>
					<input type="text" name="" id="input1">
				</li>
				<li>
					<label for="input2">도번명</label>
					<input type="text" name="" id="input2">
				</li>
				<li>
					<label for="input3">사업장</label>
					<select id="input3"><option>1사업장</option></select>
				</li>
				<li>
					<label for="input4">구매담당자</label>
					<select id="input4"><option>로그인사용자</option></select>
				</li>
				<li>
					<label for="input5">CC번호</label>
					<input type="text" name="" id="input5">
				</li>
				<li>
					<label for="input6">기종코드</label>
					<select id="input6"><option>전체</option></select>
				</li>
				<li>
					<label for="input7">업체명</label>
					<input type="text" name="" id="input7">
				</li>
				<li>
					<label for="input8">등록자</label>
					<select id="input8"><option>전체</option></select>
				</li>
				<li>
					<label for="input9">등록일</label>
					<div class="input-append date" id="fromDate">
						<input type="text" class="ui-corner-all" name="fromDate" value="2019-08-01"/><span class="add-on"><i class="icon-th"></i></span>
					</div>
					<span class="fromTo">~</span>
					<div class="input-append date" id="toDate">
						<input type="text" class="ui-corner-all" name="toDate" value="2019-08-31"/><span class="add-on"><i class="icon-th"></i></span>
					</div>
				</li>
				<li>
					<input type="checkbox" name="input10" id="input10">
					<label for="input10">Vendor</label>
				</li>
				<li>
					<input type="checkbox" name="input11" id="input11">
					<label for="input11">최종 Rev.</label>
				</li>
			</ul>
			<div class="btnBox">
				<button class="ui-button ui-corner-all searchBtn">조회</button>
			</div>
		</div>
		<div class="btnArea">
			<div class="left">
				<button class="ui-button ui-corner-all ">엑셀</button>
				<button class="ui-button ui-corner-all ">일괄검색</button>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all ">Viewing</button>
				<button class="ui-button ui-corner-all ">업체지정</button>
				<button class="ui-button ui-corner-all ">배포승인요청</button>
				<button class="ui-button ui-corner-all ">출력승인요청</button>
			</div>
		</div>
		<div class="gridArea whole">
			<div class="gridContainer">
				<table id="jqGrid3"></table>
				<div id="pagerArea"></div>
				<div id="paginate"></div>
			</div>
		</div>
	</div>
</div>
</body>
</html>