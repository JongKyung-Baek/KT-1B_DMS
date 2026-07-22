<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 생산기술자료 접수자조회 탭팝업 -->
<script>
	$(document).ready(function(){
		
		var $tab = $('.tabArea ul > li').on('click', function() { // show content that matches the index
		  var idx = $tab.index(this);
		  $('.tabArea ul > li').removeClass('current');
		  $('.tabArea ul > li').eq(idx).addClass('current');
		  $('.tabContentArea > li').stop().removeClass('current');
		  $('.tabContentArea > li').eq(idx).stop().addClass('current');
		});
		
		slideW = $('.tabArea > ul').width();		
		current = 0;
		moveL = $('.tabArea > ul').position().left;	
		$('.tabBtnArea .prevTabBtn').on('click', function(e) { // prevTabBtn - change margin Left
			e.preventDefault();
			if (current > 0 && current <= $('.tabArea > ul li').length - 1 && moveL < 0){
				current--;	
				moveL = moveL + $('.tabArea > ul li').eq(current).outerWidth(true);				
				$('.tabArea > ul').animate({left: moveL + 'px'}, 300);		
			}		
		});		
		$('.tabBtnArea .nextTabBtn').on('click', function(e) { // nextTabBtn - change margin Left
			e.preventDefault();	
			if (current < $('.tabArea > ul li').length - 1 && -moveL < slideW/4){	
				current++;	
				moveL = moveL - $('.tabArea > ul li').eq(current).outerWidth(true);
				$('.tabArea > ul').animate({left: moveL + 'px'}, 300);			
			}			
		});
		
	});
	$(function() {	
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['기종','자료유형','자료번호','문서명','REV','매수','배포매수'],
			colModel:[
				{name:'col1', index:'col1', width: "120"},
				{name:'col2', index:'col2', width: "130"},
				{name:'col3', index:'col3', width: "120"},
				{name:'col4', index:'col4', width: "120"},
				{name:'col5', index:'col5', width: "120"},
				{name:'col6', index:'col6', width: "120"},
				{name:'col7', index:'col7', width: "120"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata1 = [
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-1",col4:"Cargo-1",col5:"0",col6:"30",col7:"30"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-2",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-3",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-4",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-5",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			];
		for(var i=0;i<=mydata1.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata1[i]);
		}
		

		$("#jqGridD2").jqGrid({
			datatype: "local",
			colNames:['기종','자료유형','자료번호','문서명','REV','매수','배포매수'],
			colModel:[
				{name:'col1', index:'col1', width: "120"},
				{name:'col2', index:'col2', width: "130"},
				{name:'col3', index:'col3', width: "120"},
				{name:'col4', index:'col4', width: "120"},
				{name:'col5', index:'col5', width: "120"},
				{name:'col6', index:'col6', width: "120"},
				{name:'col7', index:'col7', width: "120"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata2 = [
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-1",col4:"Cargo-1",col5:"0",col6:"30",col7:"30"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-2",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			];
		for(var i=0;i<=mydata2.length;i++){
			$("#jqGridD2").jqGrid('addRowData',i+1,mydata2[i]);
		}
		
		$("#jqGridD3").jqGrid({
			datatype: "local",
			colNames:['기종','자료유형','자료번호','문서명','REV','매수','배포매수'],
			colModel:[
				{name:'col1', index:'col1', width: "120"},
				{name:'col2', index:'col2', width: "130"},
				{name:'col3', index:'col3', width: "120"},
				{name:'col4', index:'col4', width: "120"},
				{name:'col5', index:'col5', width: "120"},
				{name:'col6', index:'col6', width: "120"},
				{name:'col7', index:'col7', width: "120"},
			],
			width : null,
			height : null,
			autoheight : true,
			shrinkToFit : false,
			rowNum : 15,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata3 = [
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-1",col4:"Cargo-1",col5:"0",col6:"30",col7:"30"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-2",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-3",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-4",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			{col1:"XK56",col2:"용접공정서",col3:"K56W0900-5",col4:"Cargo-1",col5:"0",col6:"30",col7:"20"},
			];
		for(var i=0;i<=mydata3.length;i++){
			$("#jqGridD3").jqGrid('addRowData',i+1,mydata3[i]);
		}
	});
</script>	
<style>
</style>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">요청번호</label>
			<div class="textOnly">KM19-0140</div>
			<label for="">요청일</label>
			<div class="textOnly">2019-10-02 14:12</div>
		</li>
		<li class="half">
			<label for="">요청자</label>
			<div class="textOnly">마곡사업장 생산기술팀 양광필 차장</div>
			<label for="">다음업무 담당자</label>
			<div class="textOnly">마곡사업장 생산기술팀 양광필 차장</div>
		</li>
		<li class="half">
			<label for="">배포구분</label>
			<div class="textOnly">MDP 전자배포(생기)</div>
			<label for="">사업구분</label>
			<div class="textOnly">기타</div>
		</li>
		<li>
			<label for="">제목</label>
			<div class="textOnly">K105A1 기술변경(ECM190225) 반영 조립공정서 배포</div>
		</li>
		<li>
			<label for="">내용</label>
			<div class="textAreaOnly">text length test text length test text length test text length test text length test text length test 
			text length test text length test text length test text length test text length test text length test 
			text length test text length test text length test text length test text length test text length test 
			text length test text length test text length test text length test text length test text length test text length test</div>
		</li>
	</ul>
	<div class="tabSection">
		<div class="tabBtnArea">
			<button class="prevTabBtn">이전</button>
			<div class="tabArea">
				<ul>
					<li class="current">배포 정보</li>
					<li>배포 승인</li>
					<li>양광필1</li>
					<li>양광필2</li>
					<li>양광필3</li>
					<li>양광필4</li>
					<li>양광필5</li>
					<li>양광필6</li>
					<li>양광필7</li>
					<li>양광필8</li>
					<li>양광필9</li>
					<li>양광필10</li>
				</ul>
			</div>
			<button class="nextTabBtn">다음</button>
		</div>		
		<ul class="tabContentArea">		
			<li class="current"> <!-- 배포 정보 -->
				<ul class="section">
					<li class="half">
						<label for="">요청번호</label>
						<div class="textOnly">KM19-0140</div>
						<label for="">배포일</label>
						<div class="textOnly">2019-10-02 14:19</div>
					</li>
					<li class="half">
						<label for="">배포자</label>
						<div class="textOnly">마곡사업장 생산기술팀 양광필 차장</div>
						<label for="">다음업무 담당자</label>
						<div class="textOnly">2</div>
					</li>
					<li class="half">
						<label for="">배포구분</label>
						<div class="textOnly">사내</div>
						<label for="">배포업체</label>
						<div class="textOnly"></div>
					</li>
					<li>
						<label for="">배포요청 사유</label>
						<div class="textAreaOnly">기술변경 반영 및 조립공정서 보완사항<br>K105A1 조립공정서 개정본을 첨부와 같이 배포하오니 후속 업무진행 참조바랍니다.</div>
					</li>
				</ul>
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="sectTitle">배포자료 목록</span><span class="listCount">5</span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id=jqGridD1></table>
					</div>
				</div>
			</li>			
			<li> <!-- 배포 승인 -->
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="sectTitle">배포승인 목록</span><span class="listCount">2</span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id=jqGridD2></table>
					</div>
				</div>
			</li>			
			<li> <!-- 접수/폐기 -->
				<ul class="section">
					<li class="half">
						<label for="">요청번호</label>
						<div class="textOnly">KM19-0140</div>
						<label for="">접수일</label>
						<div class="textOnly">2019-10-11 18:17</div>
					</li>
					<li class="half">
						<label for="">접수자</label>
						<div class="textOnly">마곡사업장 공정품질팀 민경희 기감</div>
					</li>
					<li class="half">
						<label for="">교육일자</label>
						<div class="textOnly">2019-10-31</div>
						<label for="">교육대상</label>
						<div class="textOnly">PLANO MILLER 작업자 2명</div>
					</li>
					<li>
						<label for="">교육사유</label>
						<div class="textOnly">설계 변경 및 공정 개선(ECN000019)</div>
					</li>
					<li>
						<label for="">교육결과</label>
						<div class="textAreaOnly">공정서 변경 내용 확인<br>Program 및 형상 변경 확인</div>
					</li>
				</ul>
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="sectTitle">배포자료 목록</span><span class="listCount">5</span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id=jqGridD3></table>
					</div>
				</div>
				<ul class="section">
					<li class="half">
						<label for="">배포자료확인</label>
						<div class="textOnly">이상없음</div>
						<label for="">폐기유무</label>
						<div class="textOnly">폐기필요</div>
					</li>
				</ul>
			</li>
			<li>접수/폐기2</li>
			<li>접수/폐기3</li>
			<li>접수/폐기4</li>
			<li>접수/폐기5</li>
			<li>접수/폐기6</li>
			<li>접수/폐기7</li>
			<li>접수/폐기8</li>
			<li>접수/폐기9</li>
			<li>접수/폐기10</li>				
		</ul>	
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>
