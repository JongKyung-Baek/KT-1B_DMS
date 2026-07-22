<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/production/request/printRequestPopup.js"></script>
<style>
    .productionPrintRequestPopup .textCaution {
        margin: 0 0 8px 0;
        color: #7a7f93;
        font-size: 13px;
        line-height: 1.4;
        text-align: center;
    }
</style>

<div class="dialogContent commonPrintRequestPopup productionPrintRequestPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2>출력 승인 요청</h2>
        <p>요청 정보를 입력한 뒤 출력 승인 요청을 진행해 주세요.</p>
    </div>
    <p class="textCaution">출력기간 : 팀장 승인 후 1주일 / 출력 횟수 : 3회</p>

    <form id="formPrintRequestPopup">
        <ul class="section popupCard">
            <li>
                <custom:popupInputText name="requestUser" id="requestUser" label="form.requestUserName" value="${sessionUser.userNm }" disabled="disabled"/>
                <input type="hidden" name="requestUserCd" id="requestUserCd" value="${sessionUser.userCd }"/>
            </li>
            <li>
                <custom:popupSelectInputBox options="${teamList }" selectedValue="${teamLeaderUid }" name="teamLeader" label="form.authorizePerson" id="teamLeader"/>
            </li>
            <li>
                <custom:popupInputTextArea name="requestDesc" label="form.applyReason" rows="3" value="" id="requestDesc"/>
            </li>
        </ul>
        <input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
    </form>
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
        <custom:popupButton function="printRequestPopup()" name="reqPrintApproval" label="btn.approvalRequest" id="reqPrintApproval"/>
        <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>
