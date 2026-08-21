<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${tdmsBrand.systemName}</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0; min-height: 100vh; display: grid; place-items: center; padding: 28px;
            color: #172033; background: radial-gradient(circle at 85% 12%, #dcecff 0, transparent 34%), #f5f8fc;
            font-family: "Pretendard", "Noto Sans KR", Arial, sans-serif;
        }
        .conversion-card {
            width: min(560px, 100%); padding: 38px; border: 1px solid #dbe4f0;
            border-radius: 24px; background: rgba(255, 255, 255, .96);
            box-shadow: 0 22px 60px rgba(28, 67, 112, .14); text-align: left;
        }
        .conversion-icon {
            width: 58px; height: 58px; display: grid; place-items: center; margin-bottom: 22px;
            border-radius: 18px; color: #0b5ba5; background: #e8f2fc; font-size: 27px; font-weight: 800;
        }
        .is-failed .conversion-icon { color: #b42318; background: #fff0ee; }
        .conversion-eyebrow { margin: 0 0 13px; color: #1462a8; font-size: 13px; font-weight: 800; letter-spacing: .08em; }
        h1 { margin: 0; font-size: clamp(24px, 5vw, 34px); line-height: 1.25; }
        p { margin: 16px 0 28px; color: #626d7f; font-size: 16px; line-height: 1.75; }
        button {
            min-width: 104px; height: 44px; padding: 0 22px; border: 0; border-radius: 10px;
            color: #fff; background: #075a9f; font: inherit; font-weight: 750; cursor: pointer;
        }
        button:hover { background: #064b84; }
    </style>
</head>
<body>
<c:set var="failed" value="${conversionStatus eq 'FAILED'}"/>
<main class="conversion-card ${failed ? 'is-failed' : ''}" role="status" aria-live="polite">
    <div class="conversion-icon" aria-hidden="true">${failed ? '!' : '↻'}</div>
    <div class="conversion-eyebrow"><spring:message code="feature.pdfConversion.eyebrow"/></div>
    <c:choose>
        <c:when test="${failed}">
            <h1><spring:message code="feature.pdfConversion.failed.title"/></h1>
            <p><spring:message code="feature.pdfConversion.failed.description"/></p>
        </c:when>
        <c:otherwise>
            <h1><spring:message code="feature.pdfConversion.pending.title"/></h1>
            <p><spring:message code="feature.pdfConversion.pending.description"/></p>
        </c:otherwise>
    </c:choose>
    <button type="button" onclick="window.close()"><spring:message code="feature.pdfConversion.close"/></button>
</main>
</body>
</html>
