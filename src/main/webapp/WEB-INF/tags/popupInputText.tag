<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="label" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="id" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="value" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="maxlength" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="rows" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="placeholder" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="readOnly" required="false" rtexprvalue="true" description="" %>

<c:if test="${null == maxlength || '' == maxlength }">
	<c:set var="maxlength" value="3000"></c:set>
</c:if>
<c:if test="${null != placeholder && '' != placeholder }">
	<spring:message code="${placeholder}" var="placeholder" />
</c:if>
<label for="${id}"><spring:message code="${label }"/></label>
<div>
<input type="text" name="${name }" id="${id }" value="${fn:escapeXml(value) }" maxlength="${maxlength }" ${disabled } ${readOnly } style="width: ${width}" placeholder="${placeholder }"/>
</div>
