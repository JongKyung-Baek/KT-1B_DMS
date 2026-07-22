<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="value" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="label" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="checkedValue" required="false" rtexprvalue="true" description="체크박스 선택 여부" %>
<%@ attribute name="labelSide" required="false" rtexprvalue="true" description="라벨의 위치" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="" %>


	<c:choose>
		<c:when test="${checkedValue == value }">
			<input type="checkbox" id="${name }" name="${name }" value="${value }" checked="checked" ${disabled }>
		</c:when>
		<c:otherwise>
			<input type="checkbox" id="${name }" name="${name }" value="${value }" ${disabled }>
		</c:otherwise>
	</c:choose>
	<label for="${name }"><spring:message code="${label }"/></label>
	<script>
	$("#${name}").prettyCheckable({labelPosition: '${labelSide}'});
	</script>
