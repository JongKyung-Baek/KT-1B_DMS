<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
<html lang="${pageContext.response.locale.language}"><head><meta charset="utf-8">
<title>TDMS · <spring:message code="feature.mfa.title"/></title>
<style>
body{margin:0;background:#f2f6fb;color:#16334f;font:16px/1.6 system-ui,sans-serif;display:grid;min-height:100vh;place-items:center}
main{background:white;border:1px solid #d4deea;border-radius:16px;padding:2rem;width:min(28rem,78vw);margin:1.5rem 0}
input,button{box-sizing:border-box;padding:.7rem;width:100%;margin:.4rem 0;font:inherit}
button{background:#175395;color:white;border:0;border-radius:6px;cursor:pointer}
code{overflow-wrap:anywhere} [role=alert]{color:#922d24}
.setup-qr{border:1px solid #d4deea;border-radius:10px;padding:1rem;margin:1rem 0}
.setup-qr h2{font-size:1.05rem;margin:0}.setup-qr p{margin:.5rem 0}
.setup-qr img{display:block;width:280px;max-width:100%;height:auto;margin:.75rem auto;background:white}
.setup-manual{border:1px solid #d4deea;border-radius:10px;padding:1rem;margin:1rem 0}
.setup-manual summary{color:#175395;font-weight:600;cursor:pointer}
.setup-manual code{display:block;background:#f2f6fb;border-radius:6px;padding:.75rem;margin-top:.5rem;user-select:all}
summary:focus-visible,a:focus-visible,button:focus-visible{outline:3px solid #175395;outline-offset:3px}
</style></head><body><main><strong>TDMS</strong><h1><spring:message code="feature.mfa.title"/></h1>
<c:if test="${not empty setupKey}">
<p><spring:message code="feature.mfa.setup.choose"/></p>
<c:if test="${not empty setupQr}">
<section class="setup-qr" aria-labelledby="qr-title">
<h2 id="qr-title"><spring:message code="feature.mfa.setup.qr.title"/></h2>
<p><spring:message code="feature.mfa.setup.qr.help"/></p>
<spring:message code="feature.mfa.setup.qr.alt" var="qrAlt"/>
<img src="<c:out value='${setupQr}'/>" alt="<c:out value='${qrAlt}'/>" width="280" height="280">
</section>
</c:if>
<c:if test="${setupQrUnavailable}"><p role="status"><spring:message code="feature.mfa.setup.qr.unavailable"/></p></c:if>
<details class="setup-manual" ${empty setupQr ? 'open' : ''}>
<summary><spring:message code="feature.mfa.setup.manual"/></summary>
<p><spring:message code="feature.mfa.setup"/></p>
<p>TDMS · <c:out value="${account}"/><code><c:out value="${setupKey}"/></code></p>
</details>
<p><spring:message code="feature.mfa.setup.finish"/></p>
</c:if>
<c:if test="${param.retry != null}"><p role="alert"><spring:message code="feature.mfa.retry"/></p></c:if>
<form method="post" action="<c:url value='/login/mfa'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<label><spring:message code="feature.mfa.code"/><input name="otp" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autocomplete="one-time-code" required ${empty setupKey ? 'autofocus' : ''}></label>
<button><spring:message code="feature.mfa.verify"/></button></form>
<a href="<c:url value='/login/loginPage'/>"><spring:message code="feature.mfa.back"/></a>
</main></body></html>
