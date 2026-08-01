<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript"
    src="${pageContext.request.contextPath}/resources/js/views/general/system/menu/menuPopup.js?v=20260801.1"></script>

<div class="dialogContent commonRequestPopup menuPopup menu-permission-popup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible"
    aria-labelledby="menuPopupTitle">
    <header class="popupHero menu-permission-popup-header">
        <p class="menu-permission-popup-kicker">
            <spring:message code="form.roleGroup" text="메뉴 권한" />
        </p>
        <h2 id="menuPopupTitle">
            <spring:message code="form.menuNm" text="메뉴명" />
        </h2>
    </header>

    <form id="formPopup" autocomplete="off">
        <input type="hidden" id="menuCd" name="menuCd" value="${menuVo.menuCd}"/>
        <input type="hidden" id="parentMenuCd" name="parentMenuCd" value="${menuVo.parentMenuCd}"/>
        <input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>

        <section class="menu-permission-popup-card">
            <ul class="section popupCard popupFormGrid popup-grid-2 menu-permission-popup-grid">
                <li>
                    <custom:popupInputText name="parentMenuNm" id="parentMenuNm"
                        label="form.parentMenuNm" value="${menuVo.parentMenuNm}"
                        disabled="disabled"/>
                </li>
                <li>
                    <custom:popupInputText name="menuNm" id="menuNm"
                        label="form.menuNm" value="${menuVo.menuNm}"/>
                </li>
                <li>
                    <custom:popupInputText name="menuUrl" id="menuUrl"
                        label="form.menuUrl" value="${menuVo.menuUrl}"/>
                </li>
                <li>
                    <custom:popupInputText name="menuIcon" id="menuIcon"
                        label="form.menuIcon" value="${menuVo.menuIcon}"/>
                </li>
            </ul>
        </section>

        <section class="menu-permission-popup-card menu-permission-popup-options">
            <ul class="section popupCard popupFormGrid popup-grid-2 menu-permission-option-grid">
                <li>
                    <custom:popupCheckboxSingle name="useYn" value="Y"
                        label="form.useYn" checkedValue="${menuVo.useYn}"/>
                </li>
                <li>
                    <custom:popupCheckboxSingle name="popupYn" value="Y"
                        label="form.popupYn" checkedValue="${menuVo.popupYn}"/>
                </li>
            </ul>
        </section>
    </form>
</div>

<div class="dialogBtnSet menu-permission-dialog-actions">
    <div class="left"></div>
    <div class="right">
        <custom:popupButton function="savePopup()" name="save" label="btn.save" id="save"/>
        <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
    </div>
</div>
