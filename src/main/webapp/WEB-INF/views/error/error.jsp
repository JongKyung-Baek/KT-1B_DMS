<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<c:url var="homeUrl" value="/" />
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="${errorTitleCode}" var="errorTitle"/>
<spring:message code="${errorMessageCode}" var="errorMessage"/>
<spring:message code="${errorHelpCode}" var="errorHelp"/>
<spring:message code="feature.error.actions.aria" text="이동 메뉴" var="actionsAria"/>
<spring:message code="feature.error.status.aria" text="HTTP 상태 코드" var="statusAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="robots" content="noindex, nofollow">
	<title><c:out value="${errorTitle}" /> | <spring:message code="feature.error.brand" text="KT-1B 기술자료관리"/></title>
	<%@ include file="/WEB-INF/jspf/favicon.jspf" %>
	<link rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/error-page.css?v=20260726.1">
	<script defer
			src="${pageContext.request.contextPath}/resources/js/error-page.js?v=20260726.1"></script>
</head>
<body class="error-page" data-home-url="${homeUrl}">
	<main class="error-page__shell">
		<section class="error-card" aria-labelledby="error-title">
			<div class="error-card__accent" aria-hidden="true"></div>

			<header class="error-brand">
				<img class="error-brand__mark"
					 src="${pageContext.request.contextPath}/resources/images/favicon/favicon.svg?v=20260726"
					 width="42" height="42" alt="">
				<div>
					<strong><spring:message code="feature.error.brand" text="KT-1B 기술자료관리"/></strong>
					<span><spring:message code="feature.error.systemName" text="기술자료관리시스템"/></span>
				</div>
			</header>

			<div class="error-card__content">
				<div class="error-status-chip" aria-label="${statusAria}">
					<span class="error-status-chip__dot" aria-hidden="true"></span>
					HTTP <c:out value="${errorCode}" />
				</div>

				<div class="error-symbol" aria-hidden="true">
					<svg viewBox="0 0 24 24" focusable="false">
						<path d="M12 8v5"></path>
						<path d="M12 16.5v.1"></path>
						<path d="M10.2 3.9 2.8 17a2 2 0 0 0 1.7 3h15a2 2 0 0 0 1.7-3L13.8 3.9a2 2 0 0 0-3.6 0Z"></path>
					</svg>
				</div>

				<p class="error-card__eyebrow"><spring:message code="feature.error.eyebrow" text="요청 처리 안내"/></p>
				<h1 id="error-title"><c:out value="${errorTitle}" /></h1>
				<p class="error-card__message"><c:out value="${errorMessage}" /></p>

				<div class="error-help">
					<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
						<circle cx="12" cy="12" r="9"></circle>
						<path d="M12 10.5V16"></path>
						<path d="M12 7.5v.1"></path>
					</svg>
					<p><c:out value="${errorHelp}" /></p>
				</div>

				<div class="error-actions" aria-label="${actionsAria}">
					<a class="error-button error-button--primary" href="${homeUrl}">
						<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
							<path d="m4 11 8-7 8 7"></path>
							<path d="M6.5 9.5V20h11V9.5"></path>
							<path d="M10 20v-6h4v6"></path>
						</svg>
						<spring:message code="feature.error.action.home" text="홈으로"/>
					</a>
					<button class="error-button error-button--secondary"
							type="button" data-error-back>
						<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
							<path d="m10 6-6 6 6 6"></path>
							<path d="M4 12h16"></path>
						</svg>
						<spring:message code="feature.error.action.back" text="이전 화면"/>
					</button>
				</div>
			</div>
		</section>

		<p class="error-page__footer">
			<spring:message code="feature.error.securityNotice"
							text="화면에는 보안을 위해 상세 오류 정보가 표시되지 않습니다."/>
		</p>
	</main>
</body>
</html>
