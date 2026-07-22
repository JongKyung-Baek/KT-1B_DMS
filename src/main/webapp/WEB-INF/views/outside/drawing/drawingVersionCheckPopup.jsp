<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/approvalPopup.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/drawingRequest/drawingVersionCheckPopup.js"></script>
<!-- 배포 승인-->
<script>
    var popupGridParam;
    var popupGridId='gridDistributionApprovalPopup';
    $(function() {
        settingGrid('${gridInfo }', setPopupGridParam());
        setTimeout(ensureVersionGridHorizontalScroll, 0);
        $(window).off("resize.versionGrid").on("resize.versionGrid", function() {
            ensureVersionGridHorizontalScroll();
        });
    });

    function formatOpenView(cellValue, options, rowdata, action){
// 	return '<a onclick="openFile(\''+ rowdata["filePath"] +'\')">'+cellValue+'</a>';
        return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
    }

    function setPopupGridParam(){
        popupGridParam = {
            gridId : popupGridId,
            formId : 'formAcceptancePopup',
            url : '/outside/drawing/approvalStatus/selectDrawingPopupList',
            pagerId : '',
            size : 9999,
            page : 1,
            shrinkToFit : false,
            multiSelect : true,
            selectRowAction : 'check',
            numbering : false
        }

        return popupGridParam;
    }

    function formatAddMonth(cellValue, options, rowdata, action){
        if(undefined == cellValue){
            return '';
        }else{
            return cellValue + g_msg("msg.month");
        }
    }

    function ensureVersionGridHorizontalScroll() {
        var $grid = $("#" + popupGridId);
        var $gview = $("#gview_" + popupGridId);
        if ($grid.length === 0 || $gview.length === 0) return;

        var $bdiv = $gview.find(".ui-jqgrid-bdiv");
        var $hdiv = $gview.find(".ui-jqgrid-hdiv");
        var $hbox = $hdiv.find(".ui-jqgrid-hbox");
        var $htable = $hdiv.find(".ui-jqgrid-htable");
        var $btable = $bdiv.find(".ui-jqgrid-btable");
        var baseWidth = Math.floor($bdiv.width() || $gview.width() || 0);
        if (baseWidth <= 0) return;

        var forcedWidth = baseWidth + 320;

        if ($hbox.length) {
            $hbox[0].style.setProperty("width", forcedWidth + "px", "important");
            $hbox[0].style.setProperty("min-width", forcedWidth + "px", "important");
        }
        if ($htable.length) {
            $htable[0].style.setProperty("width", forcedWidth + "px", "important");
            $htable[0].style.setProperty("min-width", forcedWidth + "px", "important");
        }
        if ($btable.length) {
            $btable[0].style.setProperty("width", forcedWidth + "px", "important");
            $btable[0].style.setProperty("min-width", forcedWidth + "px", "important");
        }

        $bdiv[0].style.setProperty("overflow-x", "scroll", "important");
        $bdiv[0].style.setProperty("overflow-y", "auto", "important");
        $hdiv[0].style.setProperty("overflow-x", "hidden", "important");

        $bdiv.off("scroll.versionSync").on("scroll.versionSync", function() {
            $hdiv.scrollLeft(this.scrollLeft);
        });
    }

    function gridComplete() {
        var $grid = $("#" + popupGridId);
        $("#listCount").html($grid.getGridParam("records"));
        if (typeof dialogToolbarWidth === "function") {
            dialogToolbarWidth();
        }
        ensureVersionGridHorizontalScroll();
    }
</script>
<style>
    .section li textarea{height:80px;}
</style>
<div class="dialogContent drawingVersionCheckPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2>도면 버전 비교</h2>
    </div>
    <form id="formAcceptancePopup">
        <input id="objectNo" name="objectNo" value="${objectNo}" type="hidden"/>
        <input id="companyUserCd" name="companyUserCd" value="${companyUserCd}" type="hidden"/>
        <input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
        <input id="approvalLineId" name="approvalLineId" value="${data.approvalLineId}" type="hidden"/>
        <input id="requestPurposeData" name="requestPurposeData" value="${data.requestPurpose }" type="hidden" />
        <input type="hidden" id="sendEmailYn" name="sendEmailYn" value="N"/>
    </form>
    <div class="section">
        <div class="dialogToolbar">
            <div class="left">
                <span class="gridTitle">도면 목록</span><span class="listCount" id="listCount"></span>
            </div>
        </div>
        <div class="gridContainer">
            <table id="gridDistributionApprovalPopup"></table>
        </div>
    </div>
</div>
<div class="dialogBtnSet">
    <div class="left"></div>
    <div class="right">
        <%-- 요청이 아닐 경우 승인 반려 버튼 숨기기 --%>
        <c:set var="requestCd" value="${requestCd }"></c:set>
        <c:if test="${requestCd eq 'REQUEST'}">
            <%-- 승인요청 --%>
            <custom:popupButton function="saveAcceptance('A')" name="approvalRequest" label="btn.approvalRequest" id="approvalRequest"/>
            <%-- 승인반려 --%>
            <custom:popupButton function="saveAcceptance('R')" name="approvalReject" label="btn.approvalReject" id="approvalReject"/>
        </c:if>
        <button class="ui-button ui-corner-all bottomBtn versionCompareBtn" onclick="compareImageOutside()"><spring:message code="btn.compareImage" /></button>
        <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>
<!-- </div> -->
