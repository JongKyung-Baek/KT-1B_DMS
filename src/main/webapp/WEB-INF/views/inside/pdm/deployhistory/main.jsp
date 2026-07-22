<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- main -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dashboard - CollabHub</title>
<script>
	$(document).ready(function(){

		$(window).on('resize.jqGrid', function () {
			$("#jqGrid1").jqGrid('setGridWidth', $("#jqGrid1").parents('.ui-jqgrid').parent('div').width() );
			$("#jqGrid2").jqGrid('setGridWidth', $("#jqGrid2").parents('.ui-jqgrid').parent('div').width() );
		});

		$("#jqGrid1").jqGrid({
			datatype: "local",
			colNames:['자료번호','자료명','승인일'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2'},
				{name:'col3', index:'col3', width: "80"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rownumbers : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~'
		});
		var mydata1 = [
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-27 13:53:12"}, //1
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-26 09:12:48"}, //2
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-25 13:53:12"}, //3
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-24 09:12:48"}, //4
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-23 13:53:12"}, //5
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-22 09:12:48"}, //6
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-21 13:53:12"}, //7
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-20 09:12:48"}, //8
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-15 13:53:12"}, //9
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-13 09:12:48"}, //10
			];
		for(var i=0;i<=mydata1.length;i++){
			$("#jqGrid1").jqGrid('addRowData',i+1,mydata1[i]);
		}
		
		$("#jqGrid2").jqGrid({
			datatype: "local",
			colNames:['자료번호','자료명','승인일'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2'},
				{name:'col3', index:'col3', width: "80"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rownumbers : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~'
		});
		var mydata2 = [
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-27 13:53:12"}, //1
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-26 09:12:48"}, //2
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-25 13:53:12"}, //3
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-24 09:12:48"}, //4
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-23 13:53:12"}, //5
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-22 09:12:48"}, //6
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-21 13:53:12"}, //7
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-20 09:12:48"}, //8
			];
		for(var i=0;i<=mydata2.length;i++){
			$("#jqGrid2").jqGrid('addRowData',i+1,mydata2[i]);
		}
		
		$(".jqGridNotice").jqGrid({
			datatype: "local",
			colNames:['제목','등록일'],
			colModel:[
				{name:'col1', index:'col1'},
				{name:'col2', index:'col2', width: "40"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rownumbers : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~'
		});
		var mydatanotice = [
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			];
		for(var i=0;i<=mydatanotice.length;i++){
			$(".jqGridNotice").jqGrid('addRowData',i+1,mydatanotice[i]);
		}
		
		$(".jqGridQnA").jqGrid({
			datatype: "local",
			colNames:['제목','등록일'],
			colModel:[
				{name:'col1', index:'col1'},
				{name:'col2', index:'col2', width: "40"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rownumbers : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~'
		});
		var mydataqnA = [
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			];
		for(var i=0;i<=mydataqnA.length;i++){
			$(".jqGridQnA").jqGrid('addRowData',i+1,mydataqnA[i]);
		}
		
		/* ----- jqgrid height ----- */
		$('.approvedList .ui-jqgrid-bdiv').css({'height' : $('.approvedList .ui-jqgrid').outerHeight(true) - $('.approvedList .ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 
		$('.noticeList .ui-jqgrid-bdiv').css({'height' : $('.noticeList .ui-jqgrid').outerHeight(true) - $('.noticeList .ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 
		$(window).resize(function(){ // Set height value when resizing pages
			$('.approvedList .ui-jqgrid-bdiv').css({'height' : $('.approvedList .ui-jqgrid').outerHeight(true) - $('.approvedList .ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 
			$('.noticeList .ui-jqgrid-bdiv').css({'height' : $('.noticeList .ui-jqgrid').outerHeight(true) - $('.noticeList .ui-jqgrid-hdiv').outerHeight(true) + 'px'}); 
		});		
	});
</script>
<style>
</style>
</head>
<body>
	<div class="container userCubeType2"> <!-- half whole / userCubeType1(cube 3~) & userCubeType2 (cube 1~2)-->
		<div class="br"></div>
		<div class="br">
			<div class="cubeStatusArea">
				<h3 class="cubeStatus title">요청현황</h3>
				<ul>
					<li class="cubeStatus cube">
						<h5>
							<div class="left"><span>도면 자료배포</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h5>
						<div>
							<p class="today"><span>오늘 요청</span><span class="count">23건</span></p>
							<p class="week"><span>이번주 요청</span><span class="count">451건</span></p>
						</div>
					</li>
					<li class="cubeStatus cube">
						<h5>
							<div class="left"><span>문서 자료배포</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h5>
						<div>
							<p class="today"><span>오늘 요청</span><span class="count">23건</span></p>
							<p class="week"><span>이번주 요청</span><span class="count">451건</span></p>
						</div>
					</li>
				</ul>
				<!--
					<li class="cubeStatus cube">
						<h5>
							<div class="left"><span>S/W 자료배포</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h5>
						<div>
							<p class="today"><span>오늘 요청</span><span class="count">23건</span></p>
							<p class="week"><span>이번주 요청</span><span class="count">451건</span></p>
						</div>
					</li>
					<li class="cubeStatus cube">
						<h5>
							<div class="left"><span>설계변경 자료배포</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h5>
						<div>
							<p class="today"><span>오늘 요청</span><span class="count">23건</span></p>
							<p class="week"><span>이번주 요청</span><span class="count">451건</span></p>
						</div>
					</li>
				
					-->
			</div>
			<div class="approvedListArea">
				<div class="approvedList1">
					<h4>
						<div class="left"><span>요청 승인 목록</span></div>
						<div class="right"><button class="mainMoreBtn">more</button></div>
					</h4>
					<div class="approvedList list1">
						<table id="jqGrid1"></table>
					</div>
				</div>
				<div class="approvedList2">
					<h4>
						<div class="left"><span>요청 미승인 목록</span></div>
						<div class="right"><button class="mainMoreBtn">more</button></div>
					</h4>
					<div class="approvedList list2">
						<table id="jqGrid2"></table>
					</div>
				</div>
			</div>
			<div class="rightSideArea">
				<div class="myInfoArea">
					<h4>
						<div class="left"><span>홍길동님 환영합니다.</span></div>
						<div class="right"></div>
					</h4>
					<div class="nowConnection">
						<h5>현재 접속정보</h5>
						<div>
							<p class="connectionIp"><span>접속 IP</span><span class="content">192.168.0.210</span></p>
							<p class="connectionDate"><span>접속 일시</span><span class="content">2019-08-27 09:20:22</span></p>
						</div>
					</div>
					<div class="lastConnection">
						<h5>마지막 접속정보</h5>
						<div>
							<p class="connectionIp"><span>접속 IP</span><span class="content">192.168.0.210</span></p>
							<p class="connectionDate"><span>접속 일시</span><span class="content">2019-08-26 18:20:22</span></p>
						</div>
					</div>
				</div>
				<div class="noticeListArea cube">
					<div class="box tb1">
						<h4>
							<div class="left"><span class="tb1">공지사항</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h4>
						<div class="noticeList tb1">
							<table class="jqGridNotice"></table>
						</div>
					</div>
					<div class="box tb2">
						<h4>
							<div class="left"><span class="tb2">QnA</span></div>
							<div class="right"><button class="mainMoreBtn">more</button></div>
						</h4>
						<div class="noticeList tb2">
							<table class="jqGridQnA"></table>
						</div>
					</div>
				</div>

				<div class="noticeListArea tab">
					<h4>
						<div class="left"><span class="tb1">공지사항</span><span class="tb2">QnA</span></div>
						<div class="right"><button class="mainMoreBtn">more</button></div>
					</h4>
					<div class="box">
						<div class="noticeList tb1">
							<table class="jqGridNotice"></table>
						</div>
						<div class="noticeList tb2">
							<table class="jqGridQnA"></table>
						</div>
					</div>
				</div>
				<div class="guideBtnArea">
					<button class="guideBtn">파일 다운로드 수동설치 방법 보기</button>
				</div>
			</div>
		</div>
	</div>

</body>
</html>