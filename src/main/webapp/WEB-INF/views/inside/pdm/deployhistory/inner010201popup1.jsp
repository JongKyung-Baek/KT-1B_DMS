<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 도면 팝업 (배포승인요청 버튼) -->
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
	});
</script>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<label for="">업체</label>
			<div><select id=""><option value="" selected disabled hidden>선택하세요</option></select></div>
			<label for="">업체사용자</label>
			<div><select id=""><option>전체</option></select></div>
		</li>
		<li class="half">
			<label for="">Email</label>
			<div><input type="text" id=""></div>
		</li>
		<li class="half">
			<label for="">구매담당자</label>
			<div><select id=""><option>(주)와우소프트</option></select></div>
		</li>
		<li class="half">
			<label for="">결재자</label>
			<div><select id=""><option selected>김팀장</option></select></div>
			<label for="">방산기술보호책임자</label>
			<div><select id=""><option selected>김방산</option></select></div>
		</li>
	</ul>
	<ul class="section">
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
		<li class="half">
			<label for="">용도</label>
			<div><select id="" disabled><option selected>참고용</option></select></div>
			<label for="">유효기간</label>
			<div><select id=""><option value="" selected disabled hidden>선택하세요</option><option>1개월</option></select></div>
		</li>
		<li>
			<label for="">신청사유</label>
			<div><textarea id="" rows="2"></textarea></div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn">배포승인요청</button>
		<button class="ui-button ui-corner-all bottomBtn">닫기</button>
	</div>
</div>