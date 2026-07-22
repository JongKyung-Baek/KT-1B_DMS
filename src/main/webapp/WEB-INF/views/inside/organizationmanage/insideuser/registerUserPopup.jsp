<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/insideuser/registerUserPopup.js"></script>


<c:choose>
    <c:when test="${saveFlag == 'U' }">
        <c:set var="disabled" value="Y"/>
    </c:when>
    <c:otherwise>
        <c:set var="disabled" value="N"/>
    </c:otherwise>
</c:choose>
<div class="dialogContent registerUserPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
    <div class="popupHero">
        <h2>
            <c:choose>
                <c:when test="${saveFlag == 'U' }">내부 사용자 수정</c:when>
                <c:otherwise>내부 사용자 등록</c:otherwise>
            </c:choose>
        </h2>
        <p>내부 사용자 기본 정보와 권한 항목을 입력하거나 수정할 수 있습니다.</p>
    </div>
    <form id="formPopup">
        <input type="hidden" id="userCd" name="userCd" value="${info.userCd}"/>
        <input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>
        <ul class="section popupCard popupFormGrid popup-grid-2 popup-gap-lg">

            <li>
                <custom:popupInputText id="user_Id" name="user_Id" label="form.userId" value="${info.userId}" maxlength="40"/>
            </li>

<%--            <li>--%>
<%--                <custom:popupInputPwd id="userPwd" name="userPwd" label="form.pwd" value="" maxlength="40"  />--%>
<%--            </li>--%>

            <li>
                <custom:popupInputText id="user_Nm" name="user_Nm" label="form.userNm" value="${info.userNm}" maxlength="40" placeholder="msg.exEsobuser"/>
            </li>

            <li>
                <custom:popupInputText id="email" name="email" label="form.email" value="${info.email}" maxlength="256"/>
            </li>

            <%-- 팀 목록 --%>
            <li>
                <custom:popupSearchSelectBox name="deptCd" id="deptCd" options="${noSelect_dept}" searchUrl="/inside/authorization/selectDeptCombo" label="form.deptNm" selectedValue="${info.deptCd }" selectedText="${info.deptNm }"/>
            </li>

            <%-- 역할 목록 --%>
            <li>
                <custom:popupSearchSelectBox name="positionCd" id="positionCd" options="${noSelect_position}" searchUrl="/inside/authorization/selectPositionCombo" label="grid.positionNm" selectedValue="${info.positionCd }" selectedText="${info.positionNm }"/>
            </li>

            <%-- 사용자 권한 목록 --%>
            <li>
                <custom:popupSearchSelectBox name="roleGroupCd" id="roleGroupCd" options="${noSelect_roleGroup}" searchUrl="/inside/authorization/selectRoleGroupCombo" label="form.roleGroup" selectedValue="${info.roleGroupCd }" selectedText="${info.roleGroupNm }"/>
            </li>

            <%-- 배포 권한 목록 --%>
            <!-- <li>
                <custom:popupSearchSelectBox name="distributionAuthCd" id="distributionAuthCd" options="${noSelect_distributionAuth}" searchUrl="/inside/authorization/selectDistributionAuthCombo" label="form.distributionAuth" selectedValue="${info.distributionAuthCd }" selectedText="${info.distributionAuthNm }"/>
            </li> -->

            <%-- 사업장 목록--%>
           <!--  <li>
                <custom:popupSelectBox options="${businessAreaCd }" defaultText="form.select"  name="businessAreaCd" label="form.businessArea" id="businessAreaCd" selectedValue="${info.businessAreaCd}" />
            </li> -->


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

