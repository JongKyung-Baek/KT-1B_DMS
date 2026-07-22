<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/inside/production/history/historyDetailPopup.js"></script>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<!-- 생산기술자료 접수자조회 탭팝업 -->
<script>
var gridProductionId = 'gridProductionHistoryProduction';
var gridRequestDetailId = 'gridProductionHistoryRequestDetail';
var gridDetailInfoId = 'gridProductionHistoryDetailInfo';

var historyDetailInfo = ${historyDetailInfo};
var gridProductionInfo = '${gridProductionInfo}';
var gridRequestDetailInfo = '${gridRequestDetailInfo}';
var gridDetailInfo = '${gridDetailInfo}';
var requestDeployVoList = ${historyDetailInfo.requestDeployVoList };

var gridProductionParam;
var gridRequestDetailParam;
var gridDetailInfoParam;
var detailInfoIdx;

	$(document).ready(function(){
		setGridProductionParam();
		setGridRequestDetailParam();
		settingGridWithData('${gridProductionInfo}', gridProductionParam, 'gridProductionParam');


		var $tab = $('.tabArea ul > li').on('click', function() { // show content that matches the index
		  var idx = $tab.index(this);
		  $('.tabArea ul > li').removeClass('current');
		  $('.tabArea ul > li').eq(idx).addClass('current');
		  $('.tabContentArea > li').stop().removeClass('current');
		  $('.tabContentArea > li').eq(idx).stop().addClass('current');
		  tabClickFunc($('.tabArea ul > li').eq(idx).attr('id'));
		});

		current = 0;
		moveL = $('.tabArea > ul').position().left;
		$('.tabBtnArea .prevTabBtn').on('click', function(e) { // prevTabBtn - change margin Left
			e.preventDefault();
			if (current > 0 && current <= $('.tabArea > ul li').length && moveL < 0){
				moveL = moveL + $('.tabArea > ul li').eq(current).outerWidth(true);
				if(moveL < 99 && moveL > - 101){
					moveL = 0;
				}
				$('.tabArea > ul').animate({left: moveL + 'px'}, 300);
				current--;
			}
		});
		$('.tabBtnArea .nextTabBtn').on('click', function(e) { // nextTabBtn - change margin Left
			e.preventDefault();
			if (current < $('.tabArea > ul li').length && $('.tabArea').width() < $('.tabArea ul').width() + moveL){
				moveL = moveL - $('.tabArea > ul li').eq(current).outerWidth(true);
				$('.tabArea > ul').animate({left: moveL + 'px'}, 300);
				current++;
			}
		});
		$(".tabArea ul > li:first").trigger("click");

	});
</script>
<style>
	.dialogContent .gridContainer .ui-jqgrid{height:310px;}
</style>
<div class="dialogContent">
	<ul class="section">
		<li class="half">
			<custom:popupInputText name="requestNo" label="form.requestNo" value='${historyDetailInfo.requestNo}' id="requestNo" disabled="disabled"/>
			<custom:popupInputText name="requestDt" label="form.requestDt" value='${historyDetailInfo.requestDt}' id="requestDt" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputText name="requestUserNm" label="form.requestUserName" value='${historyDetailInfo.requestUserNm}' id="requestUserNm" disabled="disabled"/>
			<custom:popupInputText name="approvalUser" label="form.authorizePerson" value='${historyDetailInfo.approvalUser}' id="approvalUser" disabled="disabled"/>
		</li>
		<li class="half">
			<custom:popupInputText name="useEndYmd" label="form.deployDateTerm" value='${historyDetailInfo.useEndYmd}' id="useEndYmd" disabled="disabled"/>
			<custom:popupInputText name="watermarkYn" label="form.watermarkPrintYn" value='${historyDetailInfo.watermarkYn}' id="watermarkYn" disabled="disabled"/>
		</li>
		<li>
			<custom:popupInputTextArea name="requestDesc" label="form.requestReason" value="${historyDetailInfo.requestDesc }" rows="4" id="requestDesc" disabled="Y"/>
		</li>
	</ul>
	<div class="tabSection">
		<div class="tabBtnArea">
			<button class="prevTabBtn"><spring:message code='btn.prev' /></button>
			<div class="tabArea">
				<ul>
					<li id="deployInfo"><spring:message code='form.deployInfo' /></li>
					<li id="approveInfo"><spring:message code='form.approvalInfo' /></li>
					<c:forEach items="${historyDetailInfo.requestDeployVoList }" var="requestDeployVo" varStatus="status">
					<li id="${status.count }">${requestDeployVo.deployUserNm }</li>
					</c:forEach>
				</ul>
			</div>
			<button class="nextTabBtn"><spring:message code='btn.next' /></button>
		</div>
		<ul class="tabContentArea">
			<li id="deployInfo" onclick="clickDeployInfo()"> <!-- 배포 정보 -->
<!-- 				<ul class="section"> -->
<!-- 					<li class="half"> -->
<%-- 						<custom:popupInputText name="requestNo" label="form.requestNo" value='${historyDetailInfo.requestNo}' id="requestNo" disabled="disabled"/> --%>
<%-- 						<custom:popupInputText name="requestDt" label="form.requestDt" value='${historyDetailInfo.requestDt}' id="requestDt" disabled="disabled"/> --%>
<!-- 					</li> -->
<!-- 					<li class="half"> -->
<%-- 						<custom:popupInputText name="requestUserNm" label="form.requestUserName" value='${historyDetailInfo.requestUserNm}' id="requestUserNm" disabled="disabled"/> --%>
<%-- 						<custom:popupInputText name="approvalUser" label="form.authorizePerson" value='${historyDetailInfo.approvalUser}' id="approvalUser" disabled="disabled"/> --%>
<!-- 					</li> -->
<!-- 					<li class="half"> -->
<!-- 						<label for="">배포구분</label> -->
<!-- 						<div class="textOnly">사내</div> -->
<!-- 						<label for="">배포업체</label> -->
<!-- 						<div class="textOnly"></div> -->
<!-- 					</li> -->
<!-- 					<li> -->
<!-- 						<label for="">배포요청 사유</label> -->
<!-- 						<div class="textAreaOnly">기술변경 반영 및 조립공정서 보완사항<br>K105A1 조립공정서 개정본을 첨부와 같이 배포하오니 후속 업무진행 참조바랍니다.</div> -->
<!-- 					</li> -->
<!-- 				</ul> -->
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="gridTitle"><spring:message code="form.deployDocumentList" /></span><span class="listCount" id="productonListCount"></span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id='gridProductionHistoryProduction'></table>
					</div>
				</div>
			</li>
			<li> <!-- 배포 승인 -->
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="gridTitle"><spring:message code="form.distributionApprovalList" /></span><span class="listCount" id="requestListCount"></span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id='gridProductionHistoryRequestDetail'></table>
					</div>
				</div>
			</li>
			<c:forEach items="${historyDetailInfo.requestDeployVoList }" var="requestDeployVo" varStatus="status">
			<li> <!-- 접수/폐기 -->
				<ul class="section">
					<li class="half">
						<custom:popupInputText name="acceptUser${status.count }" label="form.acceptUser" value='${requestDeployVo.deployUserFullNm}' id="acceptUser${status.count }" disabled="disabled"/>
						<custom:popupInputText name="acceptDt${status.count }" label="form.acceptDt" value='${requestDeployVo.acceptDt}' id="acceptDt${status.count }" disabled="disabled"/>
					</li>
					<li class="half">
						<custom:popupInputText name="deployDt${status.count }" label="form.eduDate" value='${requestDeployVo.deployDt}' id="deployDt${status.count }" disabled="disabled"/>
						<custom:popupInputText name="deployTarget${status.count }" label="form.eduTarget" value='${requestDeployVo.deployTarget}' id="deployTarget${status.count }" disabled="disabled"/>
					</li>
					<li>
						<custom:popupInputText name="deployResult${status.count }" label="form.eduResult" value='${requestDeployVo.deployResult}' id="deployResult${status.count }" disabled="disabled"/>
					</li>
					<li>
						<custom:popupInputTextArea name="destroyInfo${status.count }" label="form.destroyInfo" value="${requestDeployVo.destroyInfo }" rows="4" id="destroyInfo${status.count }" disabled="Y"/>
					</li>
				</ul>
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="gridTitle"><spring:message code="form.deployDocumentList" /></span><span class="listCount" id="deployInfoCount${status.count }"></span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id="gridProductionHistoryDetailInfo${status.count}"></table>
					</div>
				</div>
<!-- 				<ul class="section"> -->
<!-- 					<li class="half"> -->
<!-- 						<label for="">배포자료확인</label> -->
<!-- 						<div class="textOnly">이상없음</div> -->
<!-- 						<label for="">폐기유무</label> -->
<!-- 						<div class="textOnly">폐기필요</div> -->
<!-- 					</li> -->
<!-- 				</ul> -->
			</li>
			</c:forEach>
		</ul>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
	</div>
</div>
