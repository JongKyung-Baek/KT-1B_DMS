<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="checkedValue" required="false" rtexprvalue="true" description="체크박스 선택 여부" %>
<%@ attribute name="labelSide" required="true" rtexprvalue="true" description="라벨의 위치" %>
<%@ attribute name="options" required="true" rtexprvalue="true" description="" type="java.util.List" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="" %>
<div>
<c:forEach items="${options}" var="option" >
	<c:choose>
		<c:when test="${checkedValue == option.comboVal }">
			<input type="radio" id="${name}${option.comboVal}" name="${name }" value="${option.comboVal }" checked ${disabled }>
		</c:when>
		<c:otherwise>
			<input type="radio" id="${name}${option.comboVal}" name="${name }" value="${option.comboVal }" ${disabled }>
		</c:otherwise>
	</c:choose>
	<label for="${name}${option.comboVal }">${option.comboLabel }</label>
<script>
$("#${name}${option.comboVal}").prettyCheckable({labelPosition: '${labelSide}'});
</script>
</c:forEach>
</div>