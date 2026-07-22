<%--<%@ page language="java" contentType="text/html; charset=UTF-8"--%>
<%--         pageEncoding="UTF-8"%>--%>
<%--<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>--%>
<%--<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>--%>
<%--<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>--%>
<%--<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>--%>
<%--<script type="text/javascript" src="/resources/js/views/outside/commonrequest/commonRequestPopup.js"></script>--%>
<%--<script type="text/javascript" src="/resources/js/views/inside/distribution/drawingRequest/drawingVersionCheckPopup.js"></script>--%>
<%--<script type="text/javascript" src="/resources/js/views/inside/common/clipboard.js"></script>--%>
<%--<sec:authentication property="principal" var="sessionUser" />--%>
<%--<spring:message code='form.autoIncrement' var='autoIncrement'/>--%>
<%--<script>--%>
<%--    var popupInfo = ${popupInfo};--%>
<%--    var dataType = '${dataType}';--%>
<%--    var gridInfo = ${gridInfo };--%>
<%--    $(document).ready(function(){--%>
<%--        setGridParam();--%>
<%--        settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');--%>
<%--        $("#"+popupGridId).jqGrid('clearGridData');--%>
<%--        $('#deployUserCd').attr('disabled','true');--%>
<%--    });--%>

<%--    function setGridParam(){--%>
<%--        popupGridParam = {--%>
<%--            gridId : popupGridId,--%>
<%--            multiSelect : true,--%>
<%--            numbering : false,--%>
<%--            selectRowAction : 'check',--%>
<%--            cellEdit: true,--%>
<%--            cellsubmit: 'clientArray',--%>
<%--        };--%>
<%--    }--%>
<%--</script>--%>
<%--<!-- 배포요청 팝업(배포요청 버튼) -->--%>
<%--<div class="dialogContent popup-base popup-actions-center">--%>

<%--    <div class="section">--%>
<%--        <div class="dialogToolbar">--%>
<%--            <div class="left">--%>
<%--                <span class="gridTitle"><spring:message code='${listTitle }' /></span><span class="listCount" id="listCount"></span>--%>
<%--            </div>--%>
<%--            <div class="right">--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="exportExcel()">엑셀 다운로드</button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="similar_inspection()"><spring:message code="toolbar.similar_inspection" /></button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="inspection()"><spring:message code="toolbar.inspection" /></button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="addRow()"><spring:message code="toolbar.add" /></button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="paste()"><spring:message code="toolbar.paste" /></button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="deleteRow()"><spring:message code="toolbar.delete" /></button>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <button class="ui-button ui-corner-all " onclick="deleteAllRow()"><spring:message code="toolbar.deleteAll" /></button>&ndash;%&gt;--%>
<%--            </div>--%>
<%--        </div>--%>
<%--        <div class="gridContainer">--%>
<%--            <table id="gridRequestPopup"></table>--%>
<%--        </div>--%>
<%--    </div>--%>
<%--</div>--%>
<%--<div class="dialogBtnSet">--%>
<%--    <div class="left"></div>--%>
<%--    <div class="right">--%>
<%--        <button class="ui-button ui-corner-all bottomBtn" onclick="compareImage()"><spring:message code="btn.distributionRequest" /></button>--%>
<%--        <button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>--%>
<%--    </div>--%>
<%--</div>--%>
<%--<div id="detailPopupDialog" class="dialogContainer"></div>--%>

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
        setTimeout(ensureVersionPopupGridHorizontalScroll, 0);
        $(window).off("resize.versionPopupGrid").on("resize.versionPopupGrid", function() {
            ensureVersionPopupGridHorizontalScroll();
        });
    });

    function ensureVersionPopupGridHorizontalScroll() {
        var $grid = $("#" + popupGridId);
        var $gview = $("#gview_" + popupGridId);
        var $bdiv = $gview.find(".ui-jqgrid-bdiv");
        var $hdiv = $gview.find(".ui-jqgrid-hdiv");
        var $hbox = $hdiv.find(".ui-jqgrid-hbox");
        var $htable = $hdiv.find(".ui-jqgrid-htable");
        var $btable = $bdiv.find(".ui-jqgrid-btable");
        var $headerCols = $htable.find("colgroup col");
        var $bodyCols = $btable.find("colgroup col");
        var $headerThs = $htable.find("thead tr.ui-jqgrid-labels th");
        var $firstRowTds = $btable.find("tr.jqgfirstrow td");
        var colModel = $grid.jqGrid("getGridParam", "colModel") || [];
        var baseWidth = Math.floor($bdiv.width() || $gview.width() || 0);
        var scrollWidth = 0;
        var forcedWidth = 0;
        var columnWidthTotal = 0;
        var headerCellWidthTotal = 0;
        var syncedColumnWidthTotal = 0;

        if ($grid.length === 0 || $gview.length === 0 || baseWidth <= 0) {
            return;
        }

        columnWidthTotal = colModel.reduce(function(total, column) {
            if (column && column.hidden !== true) {
                return total + (parseInt(column.width, 10) || 0);
            }
            return total;
        }, 0);
        if ($grid.jqGrid("getGridParam", "multiselect") === true) {
            columnWidthTotal += 36;
        }
        columnWidthTotal += 24;

        headerCellWidthTotal = $htable.find("th:visible").toArray().reduce(function(total, th) {
            return total + Math.ceil($(th).outerWidth() || 0);
        }, 0);

        if ($headerCols.length && $bodyCols.length) {
            $headerCols.each(function(index) {
                var $headerTh = $headerThs.eq(index);
                var width = Math.max(
                    Math.ceil(parseFloat(this.style.width) || 0),
                    Math.ceil($(this).outerWidth() || 0),
                    Math.ceil($headerTh.outerWidth() || 0)
                );
                if (width > 0) {
                    syncedColumnWidthTotal += width;
                    this.style.setProperty("width", width + "px", "important");
                    this.style.setProperty("min-width", width + "px", "important");
                    if ($bodyCols[index]) {
                        $bodyCols[index].style.setProperty("width", width + "px", "important");
                        $bodyCols[index].style.setProperty("min-width", width + "px", "important");
                    }
                    if ($firstRowTds[index]) {
                        $firstRowTds[index].style.setProperty("width", width + "px", "important");
                        $firstRowTds[index].style.setProperty("min-width", width + "px", "important");
                    }
                }
            });
        }

        scrollWidth = Math.max(
            Math.floor($bdiv[0].scrollWidth || 0),
            Math.floor($htable[0] ? $htable[0].scrollWidth || 0 : 0),
            Math.floor($btable[0] ? $btable[0].scrollWidth || 0 : 0),
            Math.floor($htable.outerWidth() || 0),
            Math.floor($btable.outerWidth() || 0),
            syncedColumnWidthTotal,
            headerCellWidthTotal,
            columnWidthTotal,
            baseWidth
        );
        forcedWidth = Math.max(baseWidth, scrollWidth);

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
        if ($bdiv.length) {
            $bdiv[0].style.setProperty("overflow-x", "scroll", "important");
            $bdiv[0].style.setProperty("overflow-y", "auto", "important");
        }
        if ($hdiv.length) {
            $hdiv[0].style.setProperty("overflow-x", "hidden", "important");
        }

        $bdiv.off("scroll.versionPopupSync").on("scroll.versionPopupSync", function() {
            $hdiv.scrollLeft(this.scrollLeft);
        });
    }

    function formatOpenView(cellValue, options, rowdata, action){
// 	return '<a onclick="openFile(\''+ rowdata["filePath"] +'\')">'+cellValue+'</a>';
        return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
    }

    function setPopupGridParam(){
        popupGridParam = {
            gridId : popupGridId,
            formId : 'formAcceptancePopup',
            url : '/inside/distribution/commonRequest/selectDrawingPopupList',
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

    function gridComplete() {
        $("#listCount").html($("#" + popupGridId).getGridParam("records"));
        setTimeout(ensureVersionPopupGridHorizontalScroll, 0);
    }
</script>
<style>

</style>
<div class="dialogContent drawingVersionCheckPopup commonRequestPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2>도면 버전 비교</h2>
        <p>이전 배포 이력과 현재 도면 정보를 확인해 주세요.</p>
    </div>
    <form id="formAcceptancePopup">
        <input id="drawingNo" name="drawingNo" value="${drawingNo}" type="hidden"/>
        <input id="objectType" name="objectType" value="${objectType}" type="hidden"/>
        <input id="approvalLineId" name="approvalLineId" value="${data.approvalLineId}" type="hidden"/>
        <input id="requestPurposeData" name="requestPurposeData" value="${data.requestPurpose }" type="hidden" />
        <input type="hidden" id="sendEmailYn" name="sendEmailYn" value="N"/>
    </form>
    <div class="section popupCard">
        <div class="dialogToolbar">
            <div class="left">
                <span class="gridTitle">도면 목록</span><span class="listCount" id="listCount"></span>
            </div>
            <!-- 			<div class="right"> -->
            <!-- 				<button class="ui-button ui-corner-all" onclick="openViewer(gridId, 'DISTRIBUTION')">Viewing</button> -->
            <!-- 			</div> -->
        </div>
        <div class="gridContainer">
            <table id="gridDistributionApprovalPopup"></table>
        </div>
    </div>
    <div class="dialogBtnSet">
        <div class="left"></div>
        <div class="right">
            <%-- 요청 상태가 아닐 경우 승인/반려 버튼 숨김 --%>
            <c:set var="requestCd" value="${requestCd }"></c:set>
            <c:if test="${requestCd eq 'REQUEST'}">
                <%-- 승인요청 --%>
                <custom:popupButton function="saveAcceptance('A')" name="approvalRequest" label="btn.approvalRequest" id="approvalRequest"/>
                <%-- 승인반려 --%>
                <custom:popupButton function="saveAcceptance('R')" name="approvalReject" label="btn.approvalReject" id="approvalReject"/>
            </c:if>
            <button class="ui-button ui-corner-all bottomBtn versionCompareBtn" onclick="compareImageInside()"><spring:message code="btn.compareImage" /></button>
            <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
        </div>
    </div>
</div>
<!-- </div> -->
