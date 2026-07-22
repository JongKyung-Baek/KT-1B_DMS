
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/inside/production/productionstatus/distributionHistoryPopup.js"></script>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<!-- 생산기술자료 자료배포이력상세조회 팝업 -->
<script>
var gridHistoryId = 'gridProductionDistributionHistory';

var gridColmodel = '${gridInfo}';
var distributionHistoryList = ${historyList };

var gridHistoryParam;
var detailInfoIdx;

	$(document).ready(function(){

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
	<div class="tabSection">
		<div class="tabBtnArea">
			<button class="prevTabBtn"><spring:message code='btn.prev' /></button>
			<div class="tabArea">
				<ul>
					<c:forEach items="${historyList }" var="historyInfo" varStatus="status">
					<li id="${status.count }">${historyInfo.deployUserFullNm }</li>
					</c:forEach>
				</ul>
			</div>
			<button class="nextTabBtn"><spring:message code='btn.next' /></button>
		</div>
		<ul class="tabContentArea">
			<c:forEach items="${historyList }" var="historyInfo" varStatus="status">
			<li> <!-- 접수/폐기 -->
				<div class="section">
					<div class="dialogToolbar">
						<div class="left">
							<span class="gridTitle"><spring:message code='form.historyList' /></span><span class="listCount" id="detailHistory${status.count }"></span>
						</div>
						<div class="right"></div>
					</div>
					<div class="gridContainer">
						<table id="gridProductionDistributionHistory${status.count}"></table>
					</div>
				</div>
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
