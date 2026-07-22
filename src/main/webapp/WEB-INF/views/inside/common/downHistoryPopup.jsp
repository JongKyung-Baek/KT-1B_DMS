<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script>
    var popupGridParam;
    $(function() {
        settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
    });


    function setPopupGridParam(){
        popupGridParam = {
            formId : 'formDownHistoryPopupList',
            gridId : 'gridDownHistoryListPopup',
            url : '/inside/distribution/downHistory/selectDownHistoryPopupList',
            size : 4,
            page : 1,
            multiSelect : false,
            numbering : false
        }
        return popupGridParam;
    }


    // 자료명: 행위이력
    function formatViewActLog(cellValue, options, rowdata, action){
        return '<a onclick="openActLogPopup_onlyForActLog(\'' + rowdata["downloadedName"] + '\')">'+cellValue+'</a>';}



    // function formatViewActLog(cellValue, options, rowdata, action){
    //     var redirectUrl = '/inside/distribution/downHistory/actLogPopup?downloadedName=' + encodeURIComponent(rowdata["downloadedName"]);
    //     return '<a href="' + redirectUrl + '">'+cellValue+'</a>';
    // }
    // function formatViewActLog(cellValue, options, rowdata, action){
    //     return '<a href="#" onclick="redirectToActLogPopup(\'' + rowdata["downloadedName"] + '\'); return false;">'+cellValue+'</a>';
    // }

</script>




<form id="formDownHistoryPopupList" name="formDownHistoryPopupList" value="다운로드 정보">
    <input type="hidden" id="objectId" name="objectId" value='${objectId}'/>
    <input type="hidden" id="requestNo" name="requestNo" value='${requestNo}'/>
</form>

<div class="dialogContent downHistoryPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2>저장이력</h2>
    </div>

    <div class="section popupCard">
        <div class="gridContainer">
            <table id="gridDownHistoryListPopup">
            </table>
        </div>
    </div>
    </div>
<div class="dialogBtnSet">
    <div class="left"></div>
    <div class="right">
        <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>
<div id="detailPopupDialog" class="dialogContainer"></div>

