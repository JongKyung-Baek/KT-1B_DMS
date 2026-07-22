<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!-- 배포이력 - 거래중단 팝업 -->
<script>
var popupGridParam;
var stopDealGridResizeNs = '.stopDealGridResize';
$(function() {
	settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
	bindStopDealGridResize();
	setTimeout(resizeStopDealGrid, 0);
	setTimeout(resizeStopDealGrid, 120);
});

function resizeStopDealGrid(){
	var $grid = $("#gridStopDealList");
	var $container = $grid.closest(".gridContainer");

	if(!$grid.length || !$container.length || !$grid[0].grid){
		return;
	}

	var containerWidth = $container.innerWidth();
	if(containerWidth > 0){
		$grid.jqGrid('setGridWidth', containerWidth, true);
	}
}

function bindStopDealGridResize(){
	$(window)
		.off('resize' + stopDealGridResizeNs)
		.on('resize' + stopDealGridResizeNs, function(){
			resizeStopDealGrid();
		});
}

function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridStopDealList',
			formId : 'formStopDeal',
			url : '/inside/distribution/history/selectCompanyList',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}

function saveStopDeal(){
	if($("#gridStopDealList").getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'), function(){
			$(this).dialog("close");
		});
		return false;
	}else {
		var list = [];
		var param = {};
		$.each($("#gridStopDealList").getGridParam('selarrrow'), function(index, item){
			var data = $("#gridStopDealList").jqGrid('getRowData', item);
			list.push(data);
		});
		param.list = list;
		callAjax(param, '/inside/distribution/history/deleteCompany', function(){
			alertMessage(g_msg('msg.complete'), function(){
				$(this).dialog("close");
				closePopup('popupDialog');
			});
		})
	}
}
</script>

<div class="dialogContent commonRequestPopup stopDealPopup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero">
		<h2>거래 중단</h2>
		<p>업체를 조회하여 선택한 뒤 거래 중단을 진행해 주세요.</p>
	</div>

	<p class="textCaution"><spring:message code="label.stopDealNotUseViewingDownload" /></p>

	<form id="formStopDeal">
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li class="half">
				<label><spring:message code="form.companyNm"/></label>
				<input id="companyNm" name="companyNm" type="text" />
			</li>
			<li class="half">
				<label><spring:message code="form.companyCd"/></label>
				<input id="bizNo" name="bizNo" type="text" />
			</li>
		</ul>
	</form>

	<div class="section popupCard">
		<div class="btnBox">
			<custom:popupButton function="searchList(popupGridParam)" name="searchPopup" label="btn.search" id="searchPopup"/>
		</div>
	</div>

	<div class="section popupCard">
		<div class="gridContainer">
			<table id="gridStopDealList"></table>
			<div id="gridStopDealListPager"></div>
		</div>
	</div>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="saveStopDeal()" name="stopDeal" label="btn.stopDeal" id="stopDeal"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
