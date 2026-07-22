<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/menu/menuPopup.js"></script>
<!-- 메뉴 팝업 -->
<div class="dialogContent commonRequestPopup menuPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
	<div class="popupHero">
		<h2><spring:message code="form.menuNm" text="메뉴 정보" /></h2>
		<p>메뉴 기본 정보와 노출 옵션을 입력하거나 수정할 수 있습니다.</p>
	</div>
	<form id="formPopup">
		<input type="hidden" id="menuCd" name="menuCd" value="${menuVo.menuCd}"/>
		<input type="hidden" id="parentMenuCd" name="parentMenuCd" value="${menuVo.parentMenuCd}"/>
		<input type="hidden" id="saveFlag" name="saveFlag" value="${saveFlag}"/>

		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li>
				<!-- 내부/외부 -->
				<custom:popupSelectBox options="${authSite }" name="authSite" label="label.authSite" id="authSite"/>
			</li>
			<li>
				<!-- 상위메뉴 -->
				<custom:popupInputText name="parentMenuNm" id="parentMenuNm" label="form.parentMenuNm" value="${menuVo.parentMenuNm }" disabled="disabled=\"disabeld\""/>
			</li>
			<li>
				<%-- 메뉴명 --%>
				<custom:popupInputText name="menuNm" id="menuNm" label="form.menuNm" value="${menuVo.menuNm }"/>
			</li>
			<li>
				<!-- 메뉴 URL -->
				<custom:popupInputText name="menuUrl" id="menuUrl" label="form.menuUrl" value="${menuVo.menuUrl }"/>
			</li>
			<li>
				<!-- 메뉴 icon -->
				<custom:popupInputText name="menuIcon" id="menuIcon" label="form.menuIcon" value="${menuVo.menuIcon }"/>
			</li>
		</ul>
		<ul class="section popupCard popupFormGrid popup-grid-2">
			<li>
				<!-- 사용여부 -->
				<custom:popupCheckboxSingle name="useYn" value="Y" label="form.useYn" checkedValue="${menuVo.useYn }"/>
			</li>
			<li>
				<!-- 팝업여부 -->
				<custom:popupCheckboxSingle name="popupYn" value="Y" label="form.popupYn" checkedValue="${menuVo.popupYn }"/>
			</li>
		</ul>
	</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<custom:popupButton function="savePopup()" name="save" label="btn.save" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
