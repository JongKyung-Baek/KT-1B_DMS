<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:url var="mobileBlockLogoUrl" value="${mobileBlockLogoPath}" />
<c:set var="mobileBlockLogoClass" value="error-brand__mark" />
<c:if test="${mobileBlockWideLogo}">
	<c:set var="mobileBlockLogoClass" value="error-brand__mark error-brand__mark--wide" />
</c:if>
<!doctype html>
<html lang="${mobileBlockLocale}">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="robots" content="noindex, nofollow, noarchive">
	<title><c:out value="${mobileBlockTitle}" /> | <c:out value="${mobileBlockBrand}" /></title>
	<%@ include file="/WEB-INF/jspf/favicon.jspf" %>
	<link rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/error-page.css?v=20260804.1">
</head>
<body class="error-page">
	<main class="error-page__shell">
		<section class="error-card" aria-labelledby="error-title">
			<div class="error-card__accent" aria-hidden="true"></div>

			<header class="error-brand">
				<c:choose>
					<c:when test="${mobileBlockAlternateBrand}">
						<img class="${mobileBlockLogoClass}"
							 src="${mobileBlockLogoUrl}?v=20260804.1"
							 alt="${mobileBlockLogoAlt}">
					</c:when>
					<c:otherwise>
						<img class="error-brand__mark"
							 src="${pageContext.request.contextPath}/resources/images/brand/kai-logo.png?v=20260802.1"
							 width="72" height="47" alt="KAI">
					</c:otherwise>
				</c:choose>
				<div>
					<strong><c:out value="${mobileBlockBrand}" /></strong>
					<span><c:out value="${mobileBlockSystemName}" /></span>
				</div>
			</header>

			<div class="error-card__content">
				<div class="error-status-chip" aria-label="${mobileBlockStatusAria}">
					<span class="error-status-chip__dot" aria-hidden="true"></span>
					HTTP 403
				</div>

				<div class="error-symbol" aria-hidden="true">
					<svg viewBox="0 0 24 24" focusable="false">
						<rect x="7" y="2.5" width="10" height="19" rx="2"></rect>
						<path d="M10 18.5h4"></path>
						<path d="m4 4 16 16"></path>
					</svg>
				</div>

				<p class="error-card__eyebrow"><c:out value="${mobileBlockEyebrow}" /></p>
				<h1 id="error-title" class="mobile-access-title">
					<span><c:out value="${mobileBlockTitleLine1}" /></span>
					<span><c:out value="${mobileBlockTitleLine2}" /></span>
				</h1>
				<p class="error-card__message"><c:out value="${mobileBlockMessage}" /></p>

				<div class="error-help">
					<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
						<circle cx="12" cy="12" r="9"></circle>
						<path d="M12 10.5V16"></path>
						<path d="M12 7.5v.1"></path>
					</svg>
					<p><c:out value="${mobileBlockHelp}" /></p>
				</div>
			</div>
		</section>

		<p class="error-page__footer"><c:out value="${mobileBlockNotice}" /></p>
	</main>
</body>
</html>
