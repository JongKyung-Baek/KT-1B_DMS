<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포승인 팝업(요청번호 상세보기) -->
<script>
	$(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#viewPrint").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#fileDistribution").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#general").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#security").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
	});
	$(function(){
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['자료명','자료유형','파일'],
			colModel:[
				{name:'col1', index:'col1', width: "250"},
				{name:'col2', index:'col2', width: "100"},
				{name:'col3', index:'col3', width: "250"},
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
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true
		});
		var mydata = [
			{col1:"",col2:"도면",col3:""},
			{col1:"",col2:"도면",col3:""},
			{col1:"",col2:"문서",col3:""},
			{col1:"",col2:"S/W",col3:""},
			{col1:"",col2:"문서",col3:""},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<style>
</style>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">업체</label>
			<div><select id="" disabled><option value="" selected>선택하세요</option></select></div>
			<label for="">업체사용자</label>
			<div><select id="" disabled><option>전체</option></select></div>
		</li>
		<li class="half">
			<label for="">Email</label>
			<div><input type="text" id="" disabled></div>
		</li>
		<li class="half">
			<label for="">구매담당자</label>
			<div><select id="" disabled><option>이길동</option></select></div>
		</li>
		<li class="half">
			<label for="">결재자</label>
			<div><select id="" disabled><option selected>김팀장</option></select></div>
		</li>
	</ul>
	<ul class="section">
		<li>
			<label for="">배포방식</label>
			<div>
				<input type="checkbox" id="viewPrint" name="distributionType" checked="checked" disabled><label for="viewPrint">Viewing/출력</label>
				<input type="checkbox" id="fileDistribution" name="distributionType" disabled><label for=fileDistribution>파일배포</label>
				<div class="fileDistributionType">
					<input type="radio" id="general" name="fileDistributionType" disabled><label for="general">일반영역</label>
					<input type="radio" id="security" name="fileDistributionType" disabled><label for="security">보안영역</label>
				</div>
			</div>
		</li>
		<li class="half">
			<label for="">용도</label>
			<div><select id="" disabled><option selected>참고용</option></select></div>
			<label for="">유효기간</label>
			<div><select id="" disabled><option value="" selected>선택하세요</option><option>1개월</option></select></div>
		</li>
		<li>
			<label for="">요청사유</label>
			<div><textarea id="" rows="2" disabled>프로젝트 진행 건으로 배포</textarea></div>
		</li>
		<li>
			<label for="">반려사유</label>
			<div><textarea id="" rows="2">프로젝트 진행 건으로 배포</textarea></div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포요청목록</span><span class="listCount">5</span>
			</div>
			<div class="right">
				<button class="ui-button ui-corner-all">Viewing</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id=jqGridD1></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">배포승인</button>
		<button class="ui-button ui-corner-all bottomBtn">배포반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>