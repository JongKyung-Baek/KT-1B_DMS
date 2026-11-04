<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
<html lang="${pageContext.response.locale.language}"><head><meta charset="utf-8">
<title>TDMS · <spring:message code="feature.mfa.title"/></title>
<style>
body{margin:0;background:#f2f6fb;color:#16334f;font:16px/1.6 system-ui,sans-serif;display:grid;min-height:100vh;place-items:center}
main{background:white;border:1px solid #d4deea;border-radius:16px;padding:2rem;width:min(25rem,78vw)}
input,button{box-sizing:border-box;padding:.7rem;width:100%;margin:.4rem 0;font:inherit}
button{background:#175395;color:white;border:0;border-radius:6px;cursor:pointer}
code{overflow-wrap:anywhere} [role=alert]{color:#922d24}
</style></head><body><main><strong>TDMS</strong><h1><spring:message code="feature.mfa.title"/></h1>
<c:if test="${not empty setupKey}"><p><spring:message code="feature.mfa.setup"/></p>
<p>TDMS · <c:out value="${account}"/><br><code><c:out value="${setupKey}"/></code></p></c:if>
<c:if test="${param.retry != null}"><p role="alert"><spring:message code="feature.mfa.retry"/></p></c:if>
<form method="post" action="<c:url value='/login/mfa'/>">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<label><spring:message code="feature.mfa.code"/><input name="otp" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autocomplete="one-time-code" required autofocus></label>
<button><spring:message code="feature.mfa.verify"/></button></form>
<a href="<c:url value='/login/loginPage'/>"><spring:message code="feature.mfa.back"/></a>
</main></body></html>
