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
            formId : 'formActLogPopupList',
            gridId : 'gridActLogListPopup',
            url : '/inside/distribution/downHistory/selectActLogPopupList',
            size : 4,
            page : 1,
            multiSelect : false,
            numbering : false
        }
        return popupGridParam;
    }
</script>
<form id="formActLogPopupList" name="formActLogPopupList" value="다운로드 정보">
    <input type="hidden" id="downloadedName" name="downloadedName" value='${downloadedName}'/>
</form>

<div class="dialogContent actLogPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2 style="padding-top: 24px;">행위이력</h2>
        <p>다운로드 이력을 확인할 수 있습니다.</p>
    </div>

    <div class="section popupCard">
        <div class="gridContainer">
            <table id="gridActLogListPopup">
            </table>
        </div>
    </div>
</div>
<div class="dialogBtnSet">
    <div class="left"></div>
    <div class="right">
        <custom:popupButton function="closePopup_onlyForActLog('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>
<div id="detailPopupDialog" class="dialogContainer"></div>

