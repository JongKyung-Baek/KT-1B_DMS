<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/insideuser/registerDeptPopup.js"></script>
<c:choose>
    <c:when test="${saveFlag == 'U' }">
        <c:set var="disabled" value="Y"/>
    </c:when>
    <c:otherwise>
        <c:set var="disabled" value="N"/>
    </c:otherwise>
</c:choose>
<div class="dialogContent registerDeptPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
    <div class="popupHero">
        <h2>부서 정보</h2>
        <p>부서 코드와 부서명, 사용 여부를 입력하거나 수정할 수 있습니다.</p>
    </div>
    <form id="formPopup">
        <input type="hidden" id="dept_cd" name="dept_cd" value="${info.deptCd}"/>
        <input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>
        <ul class="section popupCard popupFormGrid popup-grid-1">
            <c:choose>
                <c:when test="${saveFlag == 'U'}">
                    <li class="full">
                        <custom:popupInputText id="dept_cd" name="dept_cd" label="form.deptCd" value="${info.deptCd}" maxlength="40" readOnly="readOnly"/>
                    </li>
                </c:when>
            </c:choose>
            <li class="full">
                <custom:popupInputText id="dept_nm" name="dept_nm" label="form.deptName" value="${info.deptNm}" maxlength="40"/>
            </li>
            <li class="full">
                <custom:popupCheckboxSingle name="use_yn" value="Y" label="form.useYn" checkedValue="${info.useYn }"/>
            </li>
        </ul>

    </form>
</div>

<div class="dialogBtnSet">
    <div class="left"></div>
    <div class="right">
        <custom:popupButton function="saveUser()" name="approval" label="btn.save" id="save"/>
        <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>

