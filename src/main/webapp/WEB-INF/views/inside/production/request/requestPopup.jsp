<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='combo.productionDeployNew' var='deployNew'/>

<c:set var="watermarkPrintYn" value="Y"></c:set>
<c:if test="${'1210' == sessionUser.businessAreaCd }">
    <c:set var="watermarkPrintYn" value="N"></c:set>
</c:if>

<script type="text/javascript" src="/resources/js/views/inside/production/request/requestPopup.js"></script>
<script>
var gridAcceptanceId = 'gridProductionRequestAcceptance';
var gridProductionId = 'gridProductionRequestProduction';
var currentBusinessArea = '${sessionUser.businessAreaCd}';
var gridAcceptanceParam;
var gridDocumentParam;

$(document).ready(function (){
    setGridAcceptanceParam();
    setGridProductionParam();
    settingGrid('${gridAcceptance}', gridAcceptanceParam, 'gridAcceptanceParam');
    settingGridWithData('${gridProduction}', gridProductionParam, 'gridProductionParam');

    if ('1210' === currentBusinessArea) {
        $('#' + gridAcceptanceId).jqGrid('hideCol', 'copy');
        $('#' + gridAcceptanceId).jqGrid('hideCol', 'totalCount');
    }

    function applyRequestCalendarLocale() {
        var ids = ['#deployDate', '#validDate'];
        $.each(ids, function(_, selector) {
            var $cal = $(selector);
            if (!$cal.length || !$cal.data('datepicker')) return;

            $cal.datepicker('option', {
                prevText: '\uC774\uC804 \uB2EC',
                nextText: '\uB2E4\uC74C \uB2EC',
                monthNames: ['1\uC6D4','2\uC6D4','3\uC6D4','4\uC6D4','5\uC6D4','6\uC6D4','7\uC6D4','8\uC6D4','9\uC6D4','10\uC6D4','11\uC6D4','12\uC6D4'],
                monthNamesShort: ['1\uC6D4','2\uC6D4','3\uC6D4','4\uC6D4','5\uC6D4','6\uC6D4','7\uC6D4','8\uC6D4','9\uC6D4','10\uC6D4','11\uC6D4','12\uC6D4'],
                dayNames: ['\uC77C\uC694\uC77C','\uC6D4\uC694\uC77C','\uD654\uC694\uC77C','\uC218\uC694\uC77C','\uBAA9\uC694\uC77C','\uAE08\uC694\uC77C','\uD1A0\uC694\uC77C'],
                dayNamesShort: ['\uC77C','\uC6D4','\uD654','\uC218','\uBAA9','\uAE08','\uD1A0'],
                dayNamesMin: ['\uC77C','\uC6D4','\uD654','\uC218','\uBAA9','\uAE08','\uD1A0'],
                showMonthAfterYear: true,
                yearSuffix: '\uB144'
            });
        });
    }

    setTimeout(applyRequestCalendarLocale, 50);
    setTimeout(applyRequestCalendarLocale, 200);

});
</script>

<div class="dialogContent commonRequestPopup productionRequestPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2><spring:message code="btn.reqDeploymentApproval"/></h2>
        <p>요청 정보를 입력한 뒤 배포 승인 요청을 진행해 주세요.</p>
    </div>

    <form id="requestInfo" name="requestInfo">
        <ul class="section popupCard popupFormGrid popup-grid-2">
            <li class="half">
                <input type="hidden" name="deployType" id="deployType" value="NEW"/>
                <custom:popupInputText name="deployTypeNm" label="form.deployType" value="${deployNew}" id="deployTypeNm" disabled="disabled"/>
            </li>
            <li class="half">
                <input type="hidden" name="requestUser" id="requestUser" value="sessionUser.userCd"/>
                <custom:popupInputText name="requestUserNm" label="form.requestUserName" value="${sessionUser.userNm }" id="requestUserNm" readOnly="readOnly"/>
            </li>
            <li class="full">
                <custom:popupSelectInputBox options="${teamLeaderList }" selectedValue="${sessionUser.teamLeaderUid }" name="teamLeader" label="form.authorizePerson" id="teamLeader"/>
            </li>
            <li class="half">
                <custom:popupCalendar name="deployDate" label="form.deployDate" defaultDate="today" id="deployDate" maxDate="+1M" minDate="today"/>
                <div class="radioInlineSingle">
                    <label><spring:message code="form.watermarkDeployDtYn"/></label>
                    <custom:popupRadio labelSide="right" options="${watermarkDeployDtYn }" name="watermarkDeployDtYn" checkedValue="${watermarkPrintYn }"/>
                </div>
            </li>
            <li class="half">
                <custom:popupCalendar name="validDate" label="form.deployDateTerm" defaultDate="+1month" id="validDate" minDate="today"/>
                <div class="radioInlineSingle">
                    <label><spring:message code="form.watermarkPrintYn"/></label>
                    <custom:popupRadio labelSide="right" options="${watermarkYn }" name="watermarkYn" checkedValue="${watermarkPrintYn }"/>
                </div>
            </li>
            <li class="full">
                <custom:popupInputTextArea name="requestDesc" label="form.requestReason" value="" rows="3" id="requestDesc"/>
            </li>
        </ul>
        <input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
    </form>

    <div class="section popupCard">
        <div class="dialogToolbar">
            <div class="left">
                <span class="gridTitle"><spring:message code='form.deployAcceptanceList'/></span><span class="listCount" id="acceptanceListCount"></span>
            </div>
            <div class="right">
                <button class="ui-button ui-corner-all" onclick="addRow()"><spring:message code='toolbar.add'/></button>
                <button class="ui-button ui-corner-all" onclick="deleteRow()"><spring:message code='toolbar.delete'/></button>
            </div>
        </div>
        <div class="gridContainer">
            <table id="gridProductionRequestAcceptance"></table>
        </div>
    </div>

    <div class="section popupCard">
        <div class="dialogToolbar">
            <div class="left">
                <span class="gridTitle"><spring:message code='form.deployDocumentList'/></span><span class="listCount" id="productionListCount"></span>
            </div>
            <div class="right"></div>
        </div>
        <div class="gridContainer">
            <table id="gridProductionRequestProduction"></table>
        </div>
    </div>

    <div class="dialogBtnSet">
        <div class="left">
            <input type="checkbox" id="emailCheck" name="emailCheck" value="Y" checked onclick="toggleCheckboxValue('emailCheck')">
            <label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
            <script>
                $("#emailCheck").prettyCheckable({labelPosition: 'left'});
            </script>
        </div>
        <div class="right">
            <button class="ui-button ui-corner-all bottomBtn" id="reqDeploymentApproval" onclick="deployRequest()"><spring:message code='btn.productionRequest'/></button>
            <button class="ui-button ui-corner-all bottomBtn" id="close" onclick="closePopup('popupDialog')"><spring:message code='btn.close'/></button>
        </div>
    </div>
</div>
