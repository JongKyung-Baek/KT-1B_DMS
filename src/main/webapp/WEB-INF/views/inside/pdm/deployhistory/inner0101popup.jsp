<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 배포요청접수 팝업(요청번호 상세보기)-->
<script>
	$(document).ready(function(){
		$("ul select").select2({
			minimumResultsForSearch: -1
		});
		$("#viewPrint").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#fileDistribution").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#general").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
		$("#security").prettyCheckable({labelPosition:'right', customClass:'distributionMethod'});
	});
	$(function() {
		$("#jqGridD1").jqGrid({
			datatype: "local",
			colNames:['사업구분','배포유형','자료번호','REV','S/W버전','명세서버전','자료명','기종','사업장'],
			colModel:[
				{name:'col1', index:'col1', width: "90"},
				{name:'col2', index:'col2', width: "90"},
				{name:'col3', index:'col3', width: "100"},
				{name:'col4', index:'col4', width: "50"},
				{name:'col5', index:'col5', width: "90"},
				{name:'col6', index:'col6', width: "90"},
				{name:'col7', index:'col7', width: "150"},
				{name:'col8', index:'col8', width: "90"},
				{name:'col9', index:'col9', width: "90"},
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
			{col1:"양산",col2:"승인도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"개발",col2:"긴급도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"양산",col2:"원도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"개발",col2:"승인요청도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"양산",col2:"승인도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"개발",col2:"긴급도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"양산",col2:"원도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			{col1:"개발",col2:"승인요청도",col3:"A20034432",col4:"0",col5:"",col6:"",col7:"",col8:"1234",col9:"1사업장"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGridD1").jqGrid('addRowData',i+1,mydata[i]);
		}
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li>
			<label for="input1">변경요청사유</label>
			<div><textarea id="input1" rows="2"></textarea></div>
		</li>
		<li>
			<label for="input2">배포요청 반려 사유</label>
			<div><textarea id="input2" rows="2"></textarea></div>
		</li>
	</ul>
	<ul class="section">
		<li class="third">
			<label for="input1">유효기간(개월)</label>
			<div><select id="input1"><option value="" selected disabled hidden>선택하세요</option></select></div>
			<label for="input2">구매팀장 결재자</label>
			<div><select id="input2"><option value="" selected disabled hidden>선택하세요</option></select></div>
			<label for="input3">방산기술보호 책임자</label>
			<div><select id="input3"><option value="" selected disabled hidden>선택하세요</option></select></div>
		</li>
		<li>
			<label for="">배포방식</label>
			<div>
				<input type="checkbox" id="viewPrint" name="distributionType" checked="checked"><label for="viewPrint">Viewing/출력</label>
				<input type="checkbox" id="fileDistribution" name="distributionType"><label for=fileDistribution>파일배포</label>
				<div class="fileDistributionType">
					<input type="radio" id="general" name="fileDistributionType" checked disabled><label for="general">일반영역</label>
					<input type="radio" id="security" name="fileDistributionType" disabled><label for="security">보안영역</label>
				</div>
			</div>
		</li>
	</ul>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="sectTitle">배포요청 접수목록</span><span class="listCount">8</span>
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
		<button class="ui-button ui-corner-all bottomBtn">승인요청</button>
		<button class="ui-button ui-corner-all bottomBtn">승인반려</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>