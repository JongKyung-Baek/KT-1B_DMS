<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 권한설정 (decorator type = decoratorSide)-->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>권한설정 - 설정 - CollabHub</title>
<script>
	$(document).ready(function(){
		$("select").select2({
			minimumResultsForSearch: -1
		});
	});
	function openDialog() {
		openDialogPopup("/inside/pdm/deployhistory/Popup", {url:"external0801popup"}, "popupDialog", 'm', 320);
	}
</script>
<script>
	$(document).ready(function(){
		var treeParam = {
			list: [
				{id: 'T_1000', text: '자료배포', parent: '#', level: 0},
					{id: 'T_1100', text: '배포요청접수', parent: 'T_1000', level: 1},
					{id: 'T_1200', text: '검색 및 배포요청', parent: 'T_1000', level: 1},
						{id: 'T_1201', text: '도면', parent: 'T_1200', level: 2},
						{id: 'T_1202', text: '문서', parent: 'T_1200', level: 2},
						{id: 'T_1203', text: 'S/W', parent: 'T_1200', level: 2},
					{id: 'T_1300', text: '배포승인', parent: 'T_1000', level: 1},
					{id: 'T_1400', text: '배포이력', parent: 'T_1000', level: 1},
					{id: 'T_1500', text: '출력승인', parent: 'T_1000', level: 1},
					{id: 'T_1600', text: '출력이력', parent: 'T_1000', level: 1},
					{id: 'T_1700', text: '출력물 폐기승인', parent: 'T_1000', level: 1},
				
				{id: 'T_2000', text: 'CR', parent: '#', level: 0},
					{id: 'T_2001', text: 'CR 접수', parent: 'T_2000', level: 1},
					{id: 'T_2002', text: 'CR 승인', parent: 'T_2000', level: 1},
					{id: 'T_2003', text: 'CR 이력', parent: 'T_2000', level: 1},
					
				{id: 'T_3000', text: '미등록자료 배포', parent: '#', level: 0},
					{id: 'T_3001', text: '등록 및 배포요청', parent: 'T_3000', level: 1},
					{id: 'T_3002', text: '배포승인', parent: 'T_3000', level: 1},
					{id: 'T_3003', text: '배포이력', parent: 'T_3000', level: 1},

				{id: 'T_4000', text: '생산기술자료', parent: '#', level: 0},
					{id: 'T_4001', text: '검색 및 배포요청', parent: 'T_4000', level: 1},
					{id: 'T_4002', text: '배포승인', parent: 'T_4000', level: 1},
					{id: 'T_4003', text: '현장관리자 접수', parent: 'T_4000', level: 1},
					{id: 'T_4004', text: '현황 및 폐기', parent: 'T_4000', level: 1},
					{id: 'T_4005', text: '배포이력', parent: 'T_4000', level: 1},
					{id: 'T_4006', text: '출력물 폐기승인', parent: 'T_4000', level: 1},
					{id: 'T_4007', text: '사내배포현황', parent: 'T_4000', level: 1},
					
				{id: 'T_5000', text: '사용자 관리', parent: '#', level: 0},
					{id: 'T_5001', text: '내부사용자 관리', parent: 'T_5000', level: 1},
					{id: 'T_5002', text: '업체 및 사용자 관리', parent: 'T_5000', level: 1},
					{id: 'T_5003', text: '변경요청 접수', parent: 'T_5000', level: 1},
					
				{id: 'T_6000', text: '게시판', parent: '#', level: 0},
					{id: 'T_6001', text: '공지사항', parent: 'T_6000', level: 1},
					{id: 'T_6002', text: 'QnA', parent: 'T_6000', level: 1},
					
				{id: 'T_7000', text: '설정', parent: '#', level: 0},
					{id: 'T_7001', text: '권한설정', parent: 'T_7000', level: 1},
					{id: 'T_7002', text: '대결자', parent: 'T_7000', level: 1},
			]
		};
		console.log(treeParam);
		settingTree("jstree", treeParam);
		
		/* ----- container & jqgrid height(common_grid_paging.js) ----- */
		function containerHeight(){
			var tg1 = $('.bodyWrap .contentArea'); // contentArea height
			var tg1height = tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true);
			tg1.css({'height': tg1height + 'px'});
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
		<div class="btnArea">
			<div class="left"></div>
			<div class="right">
				<button class="ui-button ui-corner-all" onclick="openDialog()">그룹추가</button>
			</div>
		</div>
		<div class="listArea">
			<div class="listContainer">
				<div class="listTitle">
					<h2><span>관리그룹</span><span class="listCount">(4)</span></h2>
				</div>
				<ul>
					<li><span>시스템 관리자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>중간 관리자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li class="current"><span>구매팀</span><button type="button" class="ui-button detailBtn" onclick="openDialog()"></button></li>
					<li><span>일반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>이반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>삼반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>사반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>오반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>육반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>육일 지지</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>칠반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>팔반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
					<li><span>구반 사용자</span><button type="button" class="ui-button detailBtn"></button></li>
				</ul>
			</div>
		</div>
	</div>
	<div class="br center"></div>
	<div class="br">
		<div class="btnArea">
			<div class="left"></div>
			<div class="right">
				<label for="selectMainMenu">메인메뉴</label><select id="selectMainMenu"><option>메인메뉴 선택하기</option></select>
				<button class="ui-button ui-corner-all">저장</button>
			</div>
		</div>	
		<div class="treeArea">	
			<div id="jstree"></div>
		</div>
	</div>
	<div id="popupDialog" class="dialogContainer"></div> <!-- 그룹 추가 & 그룹 상세보기 버튼 버튼 -->
</body>
</html>
